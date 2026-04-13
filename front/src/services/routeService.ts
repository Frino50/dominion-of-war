import api from "@/services/apiService";
import RouteDto from "@/models/dtos/RouteDto.ts";

export default {
    async getAvailableRoutes(): Promise<RouteDto[]> {
        const { data } = await api.get<RouteDto[]>("/routes/available");
        return data;
    },

    async getAllRoutes(): Promise<RouteDto[]> {
        const { data } = await api.get<RouteDto[]>("/routes");
        return data;
    },

    async createRoute(payload: RouteDto): Promise<RouteDto> {
        const { data } = await api.post<RouteDto>("/routes", payload);
        return data;
    },

    async updateRoute(payload: RouteDto): Promise<RouteDto> {
        const { data } = await api.put<RouteDto>(`/routes`, payload);
        return data;
    },

    async deleteRoute(id: number): Promise<void> {
        await api.delete(`/routes/${id}`);
    },
};
