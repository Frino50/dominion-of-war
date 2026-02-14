import { Client, Message, StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { GameRoomInfo } from "@/models/dtos/GameRoomInfo.ts";
import { GameParticipantWaitingDto } from "@/models/dtos/GameParticipantWaitingDto.ts";
import { LoadoutUpdateDto } from "@/models/dtos/LoadoutUpdateDto.ts";

/**
 * Service WebSocket pour le jeu en temps réel
 */
class GameWebSocketService {
    private client: Client | null = null;
    private subscriptions: Map<string, StompSubscription> = new Map();

    /**
     * Se connecter au WebSocket
     */
    connect(token: string): Promise<void> {
        return new Promise((resolve, reject) => {
            const socket = new SockJS(
                "http://202.15.200.35:" + import.meta.env.VITE_BACK_URL + "/ws"
            );

            this.client = new Client({
                webSocketFactory: () => socket,
                connectHeaders: token
                    ? { Authorization: `Bearer ${token}` }
                    : {},
                debug: (str) => {
                    console.log("[STOMP Debug]", str);
                },
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
                onConnect: () => {
                    resolve();
                },
                onStompError: (frame) => {
                    reject(new Error(frame.headers["message"]));
                },
                onWebSocketError: (event) => {
                    reject(event);
                },
            });

            this.client.activate();
        });
    }

    /**
     * S'abonner à la liste des rooms
     */
    subscribeToRooms(callback: (rooms: GameRoomInfo[]) => void): void {
        this.subscribe("/topic/rooms", (message) => {
            const rooms: GameRoomInfo[] = JSON.parse(message.body);
            callback(rooms);
        });
    }

    /**
     * S'abonner aux participants d'une room
     */
    subscribeToParticipants(
        gameRoomId: number,
        callback: (participants: GameParticipantWaitingDto[]) => void
    ): void {
        this.subscribe(`/topic/game/${gameRoomId}/participants`, (message) => {
            const participants: GameParticipantWaitingDto[] = JSON.parse(
                message.body
            );
            callback(participants);
        });
    }

    /**
     * S'abonner aux changements de phase
     */
    subscribeToPhase(
        gameRoomId: number,
        callback: (data: { phase: string; duration?: number }) => void
    ): void {
        this.subscribe(`/topic/game/${gameRoomId}/phase`, (message) => {
            const data = JSON.parse(message.body);
            callback(data);
        });
    }

    /**
     * S'abonner aux changements de l'adversaire
     */
    subscribeToLoadout(
        gameRoomId: number,
        opponentPseudo: string,
        callback: (update: LoadoutUpdateDto) => void
    ): void {
        this.subscribe(
            `/topic/game/${gameRoomId}/${opponentPseudo}/loadout`,
            (message) => {
                const update: LoadoutUpdateDto = JSON.parse(message.body);
                callback(update);
            }
        );
    }

    /**
     * Se désabonner de tous les topics liés à une game room
     */
    unsubscribeFromGameRoom(gameRoomId: number): void {
        this.unsubscribe(`/topic/game/${gameRoomId}/participants`);
        this.unsubscribe(`/topic/game/${gameRoomId}/phase`);
        this.unsubscribe(`/topic/game/${gameRoomId}/loadout`);
    }

    /**
     * Helper pour s'abonner à un topic
     */
    private subscribe(
        destination: string,
        callback: (message: Message) => void
    ): void {
        if (!this.client?.connected) {
            return;
        }

        // Désabonner si déjà abonné
        if (this.subscriptions.has(destination)) {
            this.subscriptions.get(destination)?.unsubscribe();
        }

        const subscription = this.client.subscribe(destination, callback);
        this.subscriptions.set(destination, subscription);
    }

    /**
     * Se désabonner d'un topic spécifique
     */
    unsubscribe(destination: string): void {
        const sub = this.subscriptions.get(destination);
        if (sub) {
            sub.unsubscribe();
            this.subscriptions.delete(destination);
        }
    }

    /**
     * Vérifier si connecté
     */
    isConnected(): boolean {
        return this.client?.connected ?? false;
    }
}

export const gameWebSocket = new GameWebSocketService();
