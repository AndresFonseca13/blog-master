import instance from "./Api.ts";

import {
	type ApiResponse,
	type UpdateProfileRequest,
	type User,
} from "../types/Auth.ts";

export const getUserById = async (id: string) => {
	const response = await instance.get<ApiResponse<User>>(`/users/${id}`);
	return response.data.data;
};

export const updateProfile = async (
	id: string,
	profile: UpdateProfileRequest,
) => {
	const response = await instance.put<ApiResponse<User>>(
		`/users/${id}`,
		profile,
	);
	return response.data.data;
};
