export default interface PlayerRolesDto {
    id: string;
    pseudo: string;
    email: string;
    roleNames: string[];
    editRoles: string[];
    isEditing: boolean;
}
