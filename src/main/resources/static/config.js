env_base_config = {
     server_base_url:'http://192.168.100.33:8012/',
    //server_base_url:'http://10.150.193.231:8881/uploadManage/',
}
env_config = {
    server_base_url:env_base_config.server_base_url,
    server_preview_url:env_base_config.server_base_url + 'onlinePreview?url=',
    server_delete_url:env_base_config.server_base_url + 'deleteFile?fileName=',
}