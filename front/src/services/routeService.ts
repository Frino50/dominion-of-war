import api from "@/services/apiService";
import RouteDto from "@/models/dtos/RouteDto.ts";

export default {
    async getAvailable(): Promise<RouteDto[]> {
        const { data } = await api.get<RouteDto[]>("/routes/available");
        return data;
    },

    async getAll(): Promise<RouteDto[]> {
        const { data } = await api.get<RouteDto[]>("/routes");
        return data;
    },

    async create(payload: RouteDto): Promise<RouteDto> {
        const { data } = await api.post<RouteDto>("/routes", payload);
        return data;
    },

    async update(payload: RouteDto): Promise<RouteDto> {
        const { data } = await api.put<RouteDto>(`/routes`, payload);
        return data;
    },

    async remove(id: number): Promise<void> {
        await api.delete(`/routes/${id}`);
    },
};
