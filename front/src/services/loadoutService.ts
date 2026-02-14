import apiService from "@/services/apiService";
import type { PlayerLoadoutDto } from "@/models/dtos/PlayerLoadoutDto";
import type { LoadoutUpdateDto } from "@/models/dtos/LoadoutUpdateDto";

/**
 * Service pour gérer la sélection des unités (loadout)
 */
export default {
    /**
     * Récupérer mon loadout pour une partie
     */
    async getMyLoadout(gameRoomId: number): Promise<PlayerLoadoutDto> {
        const response = await apiService.get<PlayerLoadoutDto>(
            `/loadout/${gameRoomId}`
        );
        return response.data;
    },

    /**
     * Récupérer le statut des loadouts des adversaires
     */
    async findOpponentStatus(gameRoomId: number): Promise<LoadoutUpdateDto[]> {
        const response = await apiService.get<LoadoutUpdateDto[]>(
            `/loadout/${gameRoomId}/opponents`
        );
        return response.data;
    },

    /**
     * Sélectionner une unité par nom de sprite
     */
    async selectUnit(
        gameRoomId: number,
        spriteName: string
    ): Promise<PlayerLoadoutDto> {
        const response = await apiService.post<PlayerLoadoutDto>(
            `/loadout/${gameRoomId}/select/${spriteName}`
        );
        return response.data;
    },

    /**
     * Verrouiller mon loadout
     */
    async lockLoadout(gameRoomId: number): Promise<void> {
        await apiService.post(`/loadout/${gameRoomId}/lock`);
    },
};
