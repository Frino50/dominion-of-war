import api from "@/services/apiService";
import RoleDto from "@/models/dtos/RoleDto";

export default {
    async getAll(): Promise<string[]> {
        const { data } = await api.get<string[]>("/roles");
        return data;
    },

    async getAllAdmin(): Promise<RoleDto[]> {
        const { data } = await api.get<RoleDto[]>("/roles/all");
        return data;
    },

    async create(payload: { name: string }): Promise<RoleDto> {
        const { data } = await api.post<RoleDto>("/roles", payload);
        return data;
    },

    async update(id: number, payload: { name: string }): Promise<RoleDto> {
        const { data } = await api.put<RoleDto>(`/roles/${id}`, payload);
        return data;
    },

    async remove(id: number): Promise<void> {
        await api.delete(`/roles/${id}`);
    },
};
