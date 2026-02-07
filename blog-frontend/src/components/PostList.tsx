import { PostCard } from "./PostCard.tsx";
import type {Post} from "../types/Post.ts";

interface PostListProps {
    posts: Post[]
}

export const PostList = ({ posts }: PostListProps) => {
    if (posts.length === 0) {
        return (
            <p className="py-10 text-center text-gray-500">
                No hay posts disponibles
            </p>
        );
    }

    return (
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            {posts.map((post) => (
                <PostCard key={post.id} post={post} />
            ))}
        </div>
    );
};
