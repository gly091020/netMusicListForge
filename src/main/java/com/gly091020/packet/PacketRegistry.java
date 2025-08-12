package com.gly091020.packet;

import static com.gly091020.NetMusicList.CHANNEL;

public class PacketRegistry {
    public static void registryClient(){
        CHANNEL.registerMessage(
                3,
                PlayerPlayMusicPacket.class,
                PlayerPlayMusicPacket::encode,
                PlayerPlayMusicPacket::decode,
                ClientHandler::handleClientPlayerPlayPacket
        );

        CHANNEL.registerMessage(
                0,
                MusicListDataPacket.class,
                MusicListDataPacket::encode,
                MusicListDataPacket::decode,
                ServerHandler::handleServerMusicListDataPacket
        );
        CHANNEL.registerMessage(
                1,
                DeleteMusicDataPacket.class,
                DeleteMusicDataPacket::encode,
                DeleteMusicDataPacket::decode,
                ServerHandler::handleServerDeleteMusicDataPacket
        );
        CHANNEL.registerMessage(
                2,
                MoveMusicDataPacket.class,
                MoveMusicDataPacket::encode,
                MoveMusicDataPacket::decode,
                ServerHandler::handleServerMoveMusicDataPacket
        );
    }

    public static void registryServer(){
        CHANNEL.registerMessage(
                0,
                MusicListDataPacket.class,
                MusicListDataPacket::encode,
                MusicListDataPacket::decode,
                ServerHandler::handleServerMusicListDataPacket
        );
        CHANNEL.registerMessage(
                1,
                DeleteMusicDataPacket.class,
                DeleteMusicDataPacket::encode,
                DeleteMusicDataPacket::decode,
                ServerHandler::handleServerDeleteMusicDataPacket
        );
        CHANNEL.registerMessage(
                2,
                MoveMusicDataPacket.class,
                MoveMusicDataPacket::encode,
                MoveMusicDataPacket::decode,
                ServerHandler::handleServerMoveMusicDataPacket
        );

        CHANNEL.registerMessage(
                3,
                PlayerPlayMusicPacket.class,
                PlayerPlayMusicPacket::encode,
                PlayerPlayMusicPacket::decode,
                ServerHandler::handleServerPlayerPlayPacket
        );
    }
}
