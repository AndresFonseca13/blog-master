export interface Post {
    id: string,
    title: string,
    slug: string,
    content: string,
    summary: string,
    coverImage: string | null,
    images: string[] | null,
    videoUrls: string[] | null,
    authorId: string,
    topics: string[],
    status: PostStatus,
    createdAt: string,

}

export type PostStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED"

export interface PaginatedResponse<T>{
    content: T[],
    totalElements: number,
    totalPages: number,
    number: number,
    size: number,
    first: boolean,
    last: boolean

}