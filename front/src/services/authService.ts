import RegisterDto from "@/models/dtos/registerDto.ts";
import LoginDto from "@/models/dtos/loginDto.ts";
import apiService from "@/services/apiService.ts";

export default {
    async register(dto: RegisterDto) {
        return await apiService.post("/auth/register", dto);
    },

    async login(dto: LoginDto) {
        return await apiService.post("/auth/login", dto);
    },
};
