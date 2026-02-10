import { ParticipantRole } from "@/models/enumerations/ParticipantRole.ts";

export interface GameParticipantWaitingDto {
    pseudo: string;
    role: ParticipantRole;
}
