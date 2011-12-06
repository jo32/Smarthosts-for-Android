#!/system/bin/sh

mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system

FILEPATH=/data/data/us.bandj.jo.SmartHostAndroid/files/hosts
DESTPATH=/system/etc/hosts

rm $DESTPATH
dd if=$FILEPATH of=$DESTPATH

mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system

