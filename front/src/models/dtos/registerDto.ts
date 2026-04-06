export default class RegisterDto {
    email: string;
    pseudo: string;
    password: string;

    constructor(email: string, pseudo: string, password: string) {
        this.email = email;
        this.pseudo = pseudo;
        this.password = password;
    }
}
