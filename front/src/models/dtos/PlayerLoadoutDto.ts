import SpriteInfo from "@/models/SpriteInfos.ts";

export interface PlayerLoadoutDto {
    id: number;
    playerId: number;
    selectedUnits: SpriteInfo[];
    isLocked: boolean;
}
