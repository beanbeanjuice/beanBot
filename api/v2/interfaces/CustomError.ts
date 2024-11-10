export default class CustomError extends Error {
    status?: number;

    constructor(message: string, status?: number) {
        super(message);
        this.status = status;
    }
}

const AuthorizationError = new CustomError("Authorization failed.", 401);

export { AuthorizationError };
