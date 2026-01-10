export default interface RouteDto {
    id: number | null;
    name: string;
    componentPath: string;
    needAuth: boolean;
    roleName: string | null;
}
