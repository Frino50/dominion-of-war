export interface GameRoomInfo {
    id: number;
    name: string;
    hasPassword: boolean;
    status: string;
    playerCount: number;
    spectatorCount: number;
}
