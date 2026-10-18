use plain_server::assembly::server::ContentServer;
use std::path::PathBuf;

pub(crate) fn start(data_dir: &str, token: &str, config: &str) -> Result<String, String> {
    let mut guard = crate::ffi::server::CORE.lock().map_err(|e| e.to_string())?;
    let config: serde_json::Value = serde_json::from_str(config).map_err(|e| e.to_string())?;
    let cache_directory = config["systemProperties"]["cacheDir"]
        .as_str()
        .ok_or("Startup cache directory required")?;
    let prefs = crate::prefs()?;
    let directory = std::path::Path::new(data_dir);
    if !directory.is_absolute() {
        return Err("Absolute data directory required".into());
    }
    let paths = plain_rs::data_paths::DataPaths::new(directory);
    if prefs.path() != paths.system_prefs() || prefs.user_path() != paths.user_prefs() {
        return Err("Preferences and core must share one data directory".into());
    }
    let (cert, key) = plain_server::services::tls_identity::identity(&paths.tls_identity())?;
    if let Some(core) = guard.as_mut() {
        if core.directory != PathBuf::from(data_dir)
            || core.token != token
            || core.server.cache_paths().root() != std::path::Path::new(cache_directory)
        {
            return Err("Rust core already initialized with another session".into());
        }
        if core
            .runtime
            .block_on(core.server.public_generation())
            .is_none()
        {
            core.server
                .set_build_debug(config["debug"].as_bool().unwrap_or(false));
            let (http, _) = core.runtime.block_on(
                core.server.start_public(
                    plain_rs::prefs::user::snapshot(&prefs)
                        .map_err(|e| e.to_string())?
                        .http_port as u16,
                    plain_rs::prefs::user::snapshot(&prefs)
                        .map_err(|e| e.to_string())?
                        .https_port as u16,
                    cert,
                    key,
                ),
            )?;
            core.server.port = http;
        }
    } else {
        let runtime = tokio::runtime::Builder::new_multi_thread()
            .worker_threads(2)
            .enable_all()
            .build()
            .map_err(|e| e.to_string())?;
        let mut properties = config["systemProperties"]
            .as_object()
            .ok_or("Startup system properties required")?
            .clone();
        properties.insert("debug".into(), config["debug"].clone());
        let server = runtime.block_on(ContentServer::start_http(
            directory,
            token,
            prefs.clone(),
            properties,
            plain_rs::prefs::user::snapshot(&prefs)
                .map_err(|e| e.to_string())?
                .http_port as u16,
            plain_rs::prefs::user::snapshot(&prefs)
                .map_err(|e| e.to_string())?
                .https_port as u16,
            cert,
            key,
            config["debug"].as_bool().unwrap_or(false),
        ))?;
        crate::ffi::events::install(&runtime, &server);
        *guard = Some(crate::ffi::server::Core {
            directory: PathBuf::from(data_dir),
            token: token.into(),
            server,
            runtime,
        });
    }
    let core = guard.as_ref().unwrap();
    let (http, https) = core
        .runtime
        .block_on(core.server.http_ports())
        .ok_or("HTTP server stopped during startup")?;
    plain_rs::prefs::user::patch(
        &prefs,
        plain_rs::prefs::user::UserSettingsPatch {
            http_port: Some(http as i32),
            https_port: Some(https as i32),
            ..Default::default()
        },
    )
    .map_err(|e| e.to_string())?;
    Ok(serde_json::json!({"httpPort":http, "httpsPort":https,
        "logFile":core.server.log_file()?,
        "cacheDir":core.server.cache_paths().root(),"imageCacheDir":core.server.cache_paths().image_cache(),"videoCacheDir":core.server.cache_paths().video_cache(),
        "sharedPreviewDir":core.server.cache_paths().shared_previews(),
        "generation":core.runtime.block_on(core.server.public_generation()).ok_or("HTTP server stopped during startup")?}).to_string())
}
