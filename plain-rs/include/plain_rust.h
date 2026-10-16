#ifndef PLAIN_RUST_H
#define PLAIN_RUST_H
#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>
typedef struct { uint8_t *data; size_t len; int32_t status; } PlainBleBuffer;
uint64_t plain_ble_new(void);
uint64_t plain_ble_encoder(uint32_t id, bool response, uint32_t limit, const uint8_t *data, size_t len);
void plain_ble_free(uint64_t handle);
uint64_t plain_ble_info(uint64_t handle);
PlainBleBuffer plain_ble_call(int32_t action, uint64_t handle, uint32_t id, uint32_t sequence, uint32_t limit, bool response, const uint8_t *data, size_t len);
void plain_ble_buffer_free(PlainBleBuffer buffer);

char *plain_prefs_open(const char *data_dir, const char *cache_dir);
char *plain_prefs_user_snapshot(void);
void plain_prefs_string_free(char *pointer);


char *plain_core_start(const char *data_dir, const char *token, const char *config_json);
void plain_core_stop(void);
char *plain_http_stop(void);
char *plain_graphql(const char *request);
char *plain_resource_open(const char *id);
char *plain_resource_file(const char *id, int32_t fd, int64_t offset, int64_t length);
char *plain_resource_write(const char *id, const uint8_t *bytes, size_t len);
char *plain_resource_close(const char *id, bool failed);
char *plain_events_next(const char *stream);
char *plain_events_send(const char *action, const char *message, const uint8_t *bytes, size_t len);

#endif
