- [ ] write instruction for backup and restore
- [ ] add TOC into index

**Resilio sync backup approach**
- Create a sync folder using the Create folder option (alternatively, you can explore the Add backup option but then changes made on other devices won't be synced back to this device)
- Create a new MMX db and add it to the folder that was created in the previous step
- Go to the folder settings by pressing the i button
  - Deactivate the Selective Sync option (which then will make sure all the files in the folder are always synced)
  - Set up the Allowed Network settings if needed (note that connections are encrypted and a new peer will always need to be approved before syncing works)
  - Go to Preferences and set options as necessary. It's recommended to keep Use archive enabled, so deleted files will be kept in the archive folder until manually removed from there.
- Now start peering that folder with other devices. It's recommended to have at least two peers on different locations, and a backup process running on them keep different versions of the db.
