import apiService from "@/services/apiService";

export default {
    async spawnUnit(gameRoomId: number, spriteName: string): Promise<void> {
        await apiService.post(`/fight/${gameRoomId}/spawn/${spriteName}`);
    },
};
