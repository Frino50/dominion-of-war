import apiService from "@/services/apiService";
import { GameRoomInfo } from "@/models/dtos/GameRoomInfo.ts";
import { GameRoomDto } from "@/models/dtos/GameRoomDto.ts";
import { GameParticipantWaitingDto } from "@/models/dtos/GameParticipantWaitingDto.ts";

/**
 * Service pour les appels REST du jeu
 */
export default {
    /**
     * Récupérer toutes les parties disponibles
     */
    async listRooms(): Promise<GameRoomInfo[]> {
        const response = await apiService.get<GameRoomInfo[]>("/game/rooms");
        return response.data;
    },

    /**
     * Créer une nouvelle partie
     */
    async createRoom(dto: GameRoomDto): Promise<number> {
        const response = await apiService.post<number>("/game/create", dto);
        return response.data;
    },

    /**
     * Rejoindre une partie
     */
    async joinRoom(dto: GameRoomDto): Promise<void> {
        await apiService.post<void>("/game/join", dto);
    },

    /**
     * Récupérer les participants d'une partie
     */
    async getParticipantsWaitingDto(
        gameRoomId: number
    ): Promise<GameParticipantWaitingDto[]> {
        const response = await apiService.get<GameParticipantWaitingDto[]>(
            `/game/participants/${gameRoomId}`
        );
        return response.data;
    },

    async leaveRoom(gameRoomId: number): Promise<void> {
        await apiService.post(`/game/leave/${gameRoomId}`);
    },

    async findRoomLightDtoById(gameRoomId: number): Promise<GameRoomDto> {
        const response = await apiService.get<GameRoomDto>(
            `/game/room/${gameRoomId}`
        );
        return response.data;
    },
};
