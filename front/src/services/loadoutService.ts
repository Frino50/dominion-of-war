import apiService from "@/services/apiService";
import type { LoadoutUpdateDto } from "@/models/dtos/LoadoutUpdateDto";
import SpriteInfo from "@/models/SpriteInfos.ts";

/**
 * Service pour gérer la sélection des unités (loadout)
 */
export default {
    async getOpponent(gameRoomId: number): Promise<LoadoutUpdateDto> {
        const response = await apiService.get<LoadoutUpdateDto>(
            `/loadout/${gameRoomId}/opponents`
        );
        return response.data;
    },

    async selectUnit(
        gameRoomId: number,
        spriteName: string
    ): Promise<SpriteInfo> {
        const response = await apiService.post<SpriteInfo>(
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

    async getMyLoadout(gameRoomId: number): Promise<SpriteInfo[]> {
        const response = await apiService.get<SpriteInfo[]>(
            `/loadout/${gameRoomId}/my-loadout`
        );
        return response.data;
    },
};
