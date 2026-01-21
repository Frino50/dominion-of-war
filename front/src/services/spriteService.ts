import apiService from "@/services/apiService";
import SpriteInfo, { Hitbox } from "@/models/SpriteInfos.ts";
import ModifSpriteDto from "@/models/dtos/modifSpriteDto.ts";
import { spriteCache } from "@/services/SpriteCache.ts";

export default {
    async uploadSprite(formData: FormData) {
        return await apiService.post("/sprite", formData, {
            headers: { "Content-Type": "multipart/form-data" },
        });
    },

    async getAllSpritesInfos() {
        return await apiService.get<SpriteInfo[]>("/sprite/all");
    },

    async deleteSprite(name: string) {
        spriteCache.deleteByName(name);
        return await apiService.delete(`/sprite/delete/${name}`);
    },

    async renameSprite(modifSpriteDto: ModifSpriteDto) {
        return await apiService.put(`/sprite/rename`, modifSpriteDto);
    },

    async getImage(spritePath: string): Promise<string> {
        return spriteCache.getOrFetch(spritePath, async () => {
            const response = await apiService.get(
                `/sprite/sprite-storage/${spritePath}`,
                { responseType: "blob" }
            );
            return response.data;
        });
    },

    async getAllAnimationsBySpriteName(spriteName: string) {
        const response = await apiService.get(
            `/sprite/animations/${spriteName}`
        );
        return response.data;
    },

    async normalizeSpriteSheet(animationId: number, spriteUrl: string) {
        spriteCache.delete(spriteUrl);
        const response = await apiService.get(
            `/sprite/normalize-sprite-sheet/${animationId}`
        );
        return response.data;
    },

    async flipHorizontal(animationId: number, spriteUrl: string) {
        spriteCache.delete(spriteUrl);
        const response = await apiService.get(
            `/sprite/flip-horizontal/${animationId}`
        );
        return response.data;
    },

    async saveFrameRate(animationId: number, frameRate: number) {
        const response = await apiService.put(
            `/sprite/save-frame-rate/${animationId}/${frameRate}`
        );
        return response.data;
    },

    async saveHitbox(animationId: number, hitbox: Hitbox) {
        const response = await apiService.put(
            `/sprite/hitbox/${animationId}`,
            hitbox
        );
        return response.data;
    },

    async deleteHitbox(animationId: number) {
        const response = await apiService.delete(
            `/sprite/hitbox/${animationId}`
        );
        return response.data;
    },
};
