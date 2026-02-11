import apiService from "@/services/apiService";
import { UnitStatsDto } from "@/models/dtos/UnitStatsDto.ts";

export default {
    async getUnitStatsBySpriteName(spriteName: string): Promise<UnitStatsDto> {
        const response = await apiService.get<UnitStatsDto>(
            `/unit-stats/${spriteName}`
        );
        return response.data;
    },

    async createOrUpdateUnitStats(
        unitStats: UnitStatsDto
    ): Promise<UnitStatsDto> {
        const response = await apiService.post<UnitStatsDto>(
            `/unit-stats`,
            unitStats
        );
        return response.data;
    },
};
