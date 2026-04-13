import api from "@/services/apiService";
import PlayerRolesDto from "@/models/dtos/PlayerRolesDto.ts";

export default {
    async getAllPlayers(): Promise<PlayerRolesDto[]> {
        const { data } = await api.get<PlayerRolesDto[]>("/players");
        return data;
    },

    async updateRoles(id: string, body: string[]): Promise<PlayerRolesDto> {
        const { data } = await api.put<PlayerRolesDto>(
            `/players/${id}/roles`,
            body
        );
        return data;
    },
};
