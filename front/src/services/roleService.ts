import api from "@/services/apiService";
import RoleDto from "@/models/dtos/RoleDto";

export default {
    async getAllRoleNames(): Promise<string[]> {
        const { data } = await api.get<string[]>("/roles");
        return data;
    },

    async getAllRoles(): Promise<RoleDto[]> {
        const { data } = await api.get<RoleDto[]>("/roles/all");
        return data;
    },

    async createRole(name: string): Promise<RoleDto> {
        const { data } = await api.post<RoleDto>(`/roles/${name}`);
        return data;
    },

    async updateRole(roleDto: RoleDto): Promise<RoleDto> {
        const { data } = await api.put<RoleDto>(`/roles`, roleDto);
        return data;
    },

    async remove(id: number): Promise<void> {
        await api.delete(`/roles/${id}`);
    },
};
