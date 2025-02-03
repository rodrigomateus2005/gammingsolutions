#include <stdio.h>
#include <errno.h>
#include <math.h>
#include <signal.h>

#include <spa/param/audio/format-utils.h>

#include <pipewire/pipewire.h>

struct data
{
    struct pw_main_loop *loop;
    struct pw_stream *stream;
    struct pw_properties *props;

    struct spa_audio_info format;
    unsigned move : 1;
};

static void (*callback_ptr)(void *, int) = NULL;


static void process_buffer(void *src, int size)
{
    if (callback_ptr != NULL)
        (*callback_ptr)(src, size);

	// struct spa_pod *pod;
	// struct spa_pod_control *c;

    // void *buf;
    // int count;

	// if ((pod = spa_pod_from_data(src, n_frames, 0, n_frames)) == NULL)
	// 	return;
	// // if (!spa_pod_is_sequence(pod))
	// // 	return;

	// SPA_POD_SEQUENCE_FOREACH((struct spa_pod_sequence*)pod, c) {
    //     buf = SPA_POD_BODY(&c->value),
	// 	count = SPA_POD_BODY_SIZE(&c->value);

    //     if (callback_ptr != NULL)
    //         (*callback_ptr)(buf, count);
	// }
	// return;
}


static void on_process(void *userdata)
{
    struct data *data = userdata;
    struct pw_buffer *b;
    struct spa_buffer *buf;
    struct spa_data *d;
    float *samples, max;
    int n_frames;
    uint32_t c, n, n_channels, n_samples, peak, size;
    uint8_t *p;

    if ((b = pw_stream_dequeue_buffer(data->stream)) == NULL)
    {
        pw_log_warn("out of buffers: %m");
        return;
    }

    buf = b->buffer;
    if ((samples = buf->datas[0].data) == NULL)
        return;

    d = &buf->datas[0];

    if ((p = d->data) == NULL)
		return;

    n_channels = data->format.info.raw.channels;
    n_samples = buf->datas[0].chunk->size / sizeof(float);
    size = d->chunk->size;
    n_frames = size / d->chunk->stride;
    p += d->chunk->offset;

    process_buffer(p, d->chunk->size);

    data->move = true;
    fflush(stdout);

    pw_stream_queue_buffer(data->stream, b);
}

/* Be notified when the stream param changes. We're only looking at the
 * format changes.
 */
static void
on_stream_param_changed(void *_data, uint32_t id, const struct spa_pod *param)
{
    struct data *data = _data;

    /* NULL means to clear the format */
    if (param == NULL || id != SPA_PARAM_Format)
        return;

    if (spa_format_parse(param, &data->format.media_type, &data->format.media_subtype) < 0)
        return;

    /* only accept raw audio */
    if (data->format.media_type != SPA_MEDIA_TYPE_audio ||
        data->format.media_subtype != SPA_MEDIA_SUBTYPE_raw)
        return;


    /* call a helper function to parse the format for us. */
    if (spa_format_audio_raw_parse(param, &data->format.info.raw) < 0)
		return;

}

static const struct pw_stream_events stream_events = {
    PW_VERSION_STREAM_EVENTS,
    .param_changed = on_stream_param_changed,
    .process = on_process,
};

static void do_quit(void *userdata, int signal_number)
{
    struct data *data = userdata;
    pw_main_loop_quit(data->loop);
}

static struct data* create_pipewire_connection(void (*clb_ptr)(void *, int))
{
    const struct spa_pod *params[1];
    uint8_t buffer[1024];
    struct spa_pod_builder b = SPA_POD_BUILDER_INIT(buffer, sizeof(buffer));
    struct data *data = malloc(sizeof(struct data));
    data->loop = 0;

    callback_ptr = clb_ptr;

    pw_init(NULL, NULL);


    /* make a main loop. If you already have another main loop, you can add
     * the fd of this pipewire mainloop to it. */
    data->loop = pw_main_loop_new(NULL);

    pw_loop_add_signal(pw_main_loop_get_loop(data->loop), SIGINT, do_quit, data);
    pw_loop_add_signal(pw_main_loop_get_loop(data->loop), SIGTERM, do_quit, data);

    /* Create a simple stream, the simple stream manages the core and remote
     * objects for you if you don't need to deal with them.
     *
     * If you plan to autoconnect your stream, you need to provide at least
     * media, category and role properties.
     *
     * Pass your events and a user_data pointer as the last arguments. This
     * will inform you about the stream state. The most important event
     * you need to listen to is the process event where you need to produce
     * the data.
     */
    data->props = pw_properties_new(PW_KEY_MEDIA_TYPE, "Audio",
                              PW_KEY_MEDIA_CATEGORY, "Capture",
                              PW_KEY_MEDIA_ROLE, "Music",
                              PW_KEY_APP_NAME, "gamesolution",
                              PW_KEY_NODE_NAME, "gamesolutionnode",
                              NULL);

    // pw_properties_set(data->props, PW_KEY_FORMAT_DSP, "8 bit raw midi");

    data->stream = pw_stream_new_simple(
        pw_main_loop_get_loop(data->loop),
        "audio-capture",
        data->props,
        &stream_events,
        data);

    /* Make one parameter with the supported formats. The SPA_PARAM_EnumFormat
     * id means that this is a format enumeration (of 1 value).
     * We leave the channels and rate empty to accept the native graph
     * rate and channels. */
    params[0] = spa_format_audio_raw_build(&b, SPA_PARAM_EnumFormat,
                                           &SPA_AUDIO_INFO_RAW_INIT(
                                                   .format = SPA_AUDIO_FORMAT_F32));

    // params[0] = spa_pod_builder_add_object(&b,
	// 			SPA_TYPE_OBJECT_Format, SPA_PARAM_EnumFormat,
	// 			SPA_FORMAT_mediaType,		SPA_POD_Id(SPA_MEDIA_TYPE_application),
	// 			SPA_FORMAT_mediaSubtype,	SPA_POD_Id(SPA_MEDIA_SUBTYPE_control));


    /* Now connect this stream. We ask that our process function is
     * called in a realtime thread. */
    pw_stream_connect(data->stream,
                      PW_DIRECTION_INPUT,
                      PW_ID_ANY,
                      PW_STREAM_FLAG_AUTOCONNECT |
                          PW_STREAM_FLAG_MAP_BUFFERS |
                          PW_STREAM_FLAG_RT_PROCESS,
                      params, 1);

    return data;
}

static void run(struct data* data) {
    /* and wait while we let things run */
    pw_main_loop_run(data->loop);

    pw_stream_destroy(data->stream);
    pw_main_loop_destroy(data->loop);
    // pw_properties_free(data->props);
    pw_deinit();
    free(data);
}

static void stop(struct data* data) {
    do_quit(data, 0);
}

// int main() {
//     struct data data = create_pipewire_connection(NULL);
//     run(data);
//     return 0;
// }