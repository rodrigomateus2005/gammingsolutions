#include <jni.h>
#include <pipewire/pipewire_api.c>
#include <br_com_gammingsolution_audio_PipewireAudioBridge.h>

struct data *con;
JavaVM *jvm;
jobject listnerJava;
jclass listnerClass;

JNIEnv *getAttachedThreadEnv()
{
  JNIEnv *myNewEnv;
  JavaVMAttachArgs args;
  args.version = JNI_VERSION_1_6; // choose your JNI version
  args.name = NULL;               // you might want to give the java thread a name
  args.group = NULL;              // you might want to assign the java thread to a ThreadGroup

  int getEnvStat = (*jvm)->GetEnv(jvm, (void **)&myNewEnv, JNI_VERSION_1_6);
  if (getEnvStat == JNI_EDETACHED)
  {
    (*jvm)->AttachCurrentThread(jvm, (void **)&myNewEnv, &args);
  }
  else if (getEnvStat == JNI_OK)
  {
    //
  }
  else if (getEnvStat == JNI_EVERSION)
  {
    //
  }
  return myNewEnv;
}

void deattachThreadEnv()
{
  (*jvm)->DetachCurrentThread(jvm);
}

void data_captured(void *b, int size)
{
  JNIEnv *env;
  jobject listner;
  char *buff_conv = b;

  env = getAttachedThreadEnv();
  listner = listnerJava;

  jbyteArray ret = (*env)->NewByteArray(env, size);
  // jbyte *buf = (*env)->GetByteArrayElements(env, ret, NULL);

  (*env)->SetByteArrayRegion(env, ret, 0, size, (jbyte *)b); 


  // for (int i = 0; i < size; i++)
  // {
  //   buf[i] = (*env)-> buff_conv[i];
  // }

  // memcpy(b, buf, size);


  // for (int c = 0; c < channels; c++) {
  //   for (int n = c; n < n_samples; n += channels)
  //     max = fmaxf(max, fabsf(samples[n]));

  // for (int i = 0; i < channels; i++)
  // {
  //   for (int j = 0; j < datas[i].chunk->size; j+=datas[i].chunk->stride)
  //   {
  //     buf[i / datas[i].chunk->stride] = (uint8_t *) datas[i].data + i;
  //   }
  // }

  // jclass cls_foo = (*env)->GetObjectClass(env, listner);
  jmethodID on_process_java = (*env)->GetMethodID(env, listnerClass, "onProcess", "(I[B)V");
  (*env)->CallVoidMethod(env, listner, on_process_java, 0, ret);

  // (*env)->ReleaseByteArrayElements(env, ret, buf, 0);

  // deattachThreadEnv();
}

JNIEXPORT void JNICALL Java_br_com_gammingsolution_audio_PipewireAudioBridge_connect(JNIEnv *env, jobject jthis, jobject listner)
{
  (*env)->GetJavaVM(env, &jvm);

  listnerJava = (*env)->NewGlobalRef(env, listner);
  // Cache the Java callbacks class (in case of interface, this will be the concrete implementation class)
  jclass objClass = (*env)->GetObjectClass(env, listner);
  // Check for null
  if (objClass)
  {
    listnerClass = (jclass)((*env)->NewGlobalRef(env, objClass));
    (*env)->DeleteLocalRef(env, objClass);
  }

  con = create_pipewire_connection(&data_captured);
  run(con);

  // if (ret.size() < 1)
  //     return NULL;

  // jclass arrClass = env->FindClass("java/lang/String"); //allocate
  // jobjectArray outArray = env->NewObjectArray(ret.size(), arrClass, env->NewStringUTF(ret.at(0).c_str())); //allocate

  // return outArray;
}

JNIEXPORT void JNICALL Java_br_com_gammingsolution_audio_PipewireAudioBridge_stop(JNIEnv *env, jobject jthis)
{
  stop(con);
}
