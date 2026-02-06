export interface User{
    id: string,
    email: string,
    username: string,
    role: string,
    createdAt: string,
    provider?: string,
    profilePicture?: string,
    emailVerified?: boolean,
    providerId?: string,
}

export interface UserRegisterRequest{
    username: string,
    email: string,
    password: string,
}


export interface ApiResponse<T>{
    success: boolean,
    message: string,
    data: T,
    timestamp: string
}

export interface LoginRequest{
    username: string,
    password: string,
}

export interface LoginResponse{
    token: string,
    user: User
}

