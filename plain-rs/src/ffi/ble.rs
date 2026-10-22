//! Keep the codec implementation in plain-rs while exposing this app's JNI package.
//! The dependency still exports the old package names; its C ABI remains unchanged.

use jni::sys::{JNIEnv, jboolean, jbyteArray, jint, jlong, jobject};

unsafe extern "system" {
    #[link_name = "Java_com_ismartcoding_plain_rust_bridge_RustBleBridge_encoderNative"]
    fn encoder(
        env: *mut JNIEnv,
        object: jobject,
        data: jbyteArray,
        id: jint,
        response: jboolean,
        limit: jint,
    ) -> jlong;
    #[link_name = "Java_com_ismartcoding_plain_rust_bridge_RustBleBridge_newNative"]
    fn create(env: *mut JNIEnv, object: jobject) -> jlong;
    #[link_name = "Java_com_ismartcoding_plain_rust_bridge_RustBleBridge_freeNative"]
    fn free(env: *mut JNIEnv, object: jobject, handle: jlong);
    #[link_name = "Java_com_ismartcoding_plain_rust_bridge_RustBleBridge_infoNative"]
    fn info(env: *mut JNIEnv, object: jobject, handle: jlong) -> jlong;
    #[link_name = "Java_com_ismartcoding_plain_rust_bridge_RustBleBridge_callNative"]
    fn call(
        env: *mut JNIEnv,
        object: jobject,
        action: jint,
        handle: jlong,
        id: jint,
        sequence: jint,
        limit: jint,
        response: jboolean,
        data: jbyteArray,
    ) -> jbyteArray;
}

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_com_ismartcoding_plain_rust_native_RustBleBridge_encoderNative(
    env: *mut JNIEnv,
    object: jobject,
    data: jbyteArray,
    id: jint,
    response: jboolean,
    limit: jint,
) -> jlong {
    unsafe { encoder(env, object, data, id, response, limit) }
}

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_com_ismartcoding_plain_rust_native_RustBleBridge_newNative(
    env: *mut JNIEnv,
    object: jobject,
) -> jlong {
    unsafe { create(env, object) }
}

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_com_ismartcoding_plain_rust_native_RustBleBridge_freeNative(
    env: *mut JNIEnv,
    object: jobject,
    handle: jlong,
) {
    unsafe { free(env, object, handle) }
}

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_com_ismartcoding_plain_rust_native_RustBleBridge_infoNative(
    env: *mut JNIEnv,
    object: jobject,
    handle: jlong,
) -> jlong {
    unsafe { info(env, object, handle) }
}

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_com_ismartcoding_plain_rust_native_RustBleBridge_callNative(
    env: *mut JNIEnv,
    object: jobject,
    action: jint,
    handle: jlong,
    id: jint,
    sequence: jint,
    limit: jint,
    response: jboolean,
    data: jbyteArray,
) -> jbyteArray {
    unsafe {
        call(
            env, object, action, handle, id, sequence, limit, response, data,
        )
    }
}
