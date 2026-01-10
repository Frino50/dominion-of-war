import api from "@/services/apiService";

export default {
    async getAll(): Promise<string[]> {
        const { data } = await api.get<string[]>("/roles");
        return data;
    },
};
