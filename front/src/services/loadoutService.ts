import apiService from "@/services/apiService";
import type { PlayerLoadoutDto } from "@/models/dtos/PlayerLoadoutDto";
import type { LoadoutUpdateDto } from "@/models/dtos/LoadoutUpdateDto";
import SpriteInfo from "@/models/SpriteInfos.ts";

/**
 * Service pour gérer la sélection des unités (loadout)
 */
export default {
    /**
     * Récupérer mon loadout pour une partie
     */
    async findSpriteInfosByPlayerAndRoom(
        gameRoomId: number
    ): Promise<SpriteInfo[]> {
        const response = await apiService.get<SpriteInfo[]>(
            `/loadout/${gameRoomId}`
        );
        return response.data;
    },

    /**
     * Récupérer le statut des loadouts des adversaires
     */
    async loadOpponentStatus(gameRoomId: number): Promise<LoadoutUpdateDto> {
        const response = await apiService.get<LoadoutUpdateDto>(
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
