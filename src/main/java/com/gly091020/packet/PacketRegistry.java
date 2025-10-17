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
                6,
                PlayEnderMusicPlayerPacket.class,
                PlayEnderMusicPlayerPacket::encode,
                PlayEnderMusicPlayerPacket::decode,
                ClientHandler::handleClientEnderPlayerPlayPacket
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
        CHANNEL.registerMessage(
                4,
                UpdatePlayerMusicPacket.class,
                UpdatePlayerMusicPacket::encode,
                UpdatePlayerMusicPacket::decode,
                ServerHandler::handleServerUpdateMusicPacket
        );
        CHANNEL.registerMessage(
                5,
                UpdateMusicTickCTSPacket.class,
                UpdateMusicTickCTSPacket::encode,
                UpdateMusicTickCTSPacket::decode,
                ServerHandler::handlePlayerUpdateTickPacket
        );
        CHANNEL.registerMessage(8,
                StopMusicPacket.class,
                StopMusicPacket::encode,
                StopMusicPacket::decode,
                ClientHandler::handleStopMusicPacket
        );
        CHANNEL.registerMessage(7,
                StopMusicPacketServer.class,
                StopMusicPacketServer::encode,
                StopMusicPacketServer::decode,
                ServerHandler::handleStopMusicPacket
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

        CHANNEL.registerMessage(
                4,
                UpdatePlayerMusicPacket.class,
                UpdatePlayerMusicPacket::encode,
                UpdatePlayerMusicPacket::decode,
                ServerHandler::handleServerUpdateMusicPacket
        );

        CHANNEL.registerMessage(
                5,
                UpdateMusicTickCTSPacket.class,
                UpdateMusicTickCTSPacket::encode,
                UpdateMusicTickCTSPacket::decode,
                ServerHandler::handlePlayerUpdateTickPacket
        );

        CHANNEL.registerMessage(
                6,
                PlayEnderMusicPlayerPacket.class,
                PlayEnderMusicPlayerPacket::encode,
                PlayEnderMusicPlayerPacket::decode,
                (packet, contextSupplier) -> {}
        );
        CHANNEL.registerMessage(8,
                StopMusicPacket.class,
                StopMusicPacket::encode,
                StopMusicPacket::decode,
                (packet, contextSupplier) -> {}
        );
        CHANNEL.registerMessage(7,
                StopMusicPacketServer.class,
                StopMusicPacketServer::encode,
                StopMusicPacketServer::decode,
                ServerHandler::handleStopMusicPacket
        );
    }
}
