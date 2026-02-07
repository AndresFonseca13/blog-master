import { type Post, type PaginatedResponse} from "../types/Post.ts";
import instance from "./Api.ts";
import type {ApiResponse} from "../types/Auth.ts";

export const getPosts = async (page: number, size: number) => {

    const response = await instance.get<ApiResponse<PaginatedResponse<Post>>>('/posts', { params: {
        page: page,
        size: size,
        }});

    return response.data.data;
}

export const getPostBySlug = async (slug: string) => {
    const response = await instance.get<ApiResponse<Post>>(`/posts/${slug}`);

    return response.data.data;
}