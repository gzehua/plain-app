use super::values::{c_string, input};
use std::ffi::c_char;

fn decide(request_id: &str, allowed: bool) -> Result<(), String> {
    let (runtime, confirmation) = {
        let core = super::server::CORE
            .lock()
            .map_err(|error| error.to_string())?;
        let core = core.as_ref().ok_or("Rust core not initialized")?;
        (
            core.runtime.handle().clone(),
            core.server.web_login_confirmation(),
        )
    };
    runtime.block_on(confirmation.decide(request_id, allowed))
}

#[unsafe(no_mangle)]
pub extern "C" fn plain_web_login_decide(request_id: *const c_char, allowed: bool) -> *mut c_char {
    c_string(
        match input(request_id).and_then(|id| decide(&id, allowed)) {
            Ok(()) => String::new(),
            Err(error) => error,
        },
    )
}

#[cfg(target_os = "android")]
mod android {
    use super::*;
    use jni::{
        EnvUnowned,
        errors::ThrowRuntimeExAndDefault,
        objects::{JObject, JString},
        sys::{jboolean, jstring},
    };

    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_ismartcoding_plain_rust_bridge_RustWebLoginBridge_decideNative(
        mut env: EnvUnowned<'_>,
        _: JObject<'_>,
        request_id: JString<'_>,
        allowed: jboolean,
    ) -> jstring {
        env.with_env(|env| -> jni::errors::Result<_> {
            env.new_string(match decide(&request_id.to_string(), allowed) {
                Ok(()) => String::new(),
                Err(error) => error,
            })
        })
        .resolve::<ThrowRuntimeExAndDefault>()
        .into_raw()
    }
}
