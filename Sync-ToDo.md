* [x] Move preference URL and user into remote URL (database metafile)
* [ ] Move `isSyncCloud` check into the metafile
* [ ] Add the ability to switch from remote file to remote server (so any actual DB can be migrated to mmex-sync)
* [ ] Add a welcome message and instructions for subscription or PocketBase server setup
* [ ] Introduce an Abstract Class so `SyncManager` can reference `ISyncClient`, and implement it via `PocketBaseClient`
* [ ] Rename `PocketBaseSetupActivity` to `CloudSyncActivity` and add a service type (currently fixed to PocketBase)
* [ ] Ensure that opening a new DB clears all sync information
* [ ] Allow reopening from history to restore all information (and prompt for password)
* [ ] Design a new layout with instructions and a link to the documentation
