import { useState, useEffect} from "react";
import {getPosts } from "../services/PostService.ts";
import { PostList } from "../components/PostList.tsx";
import { Pagination} from "../components/Pagination.tsx";
import type {Post} from "../types/Post.ts";

export const Home = () => {
    const [posts, setPosts] = useState<Post[]>([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchPosts = async () => {
            setIsLoading(true);
            setError(null);
            try{
                const data = await getPosts(currentPage, 6);
                setPosts(data.content);
                setTotalPages(data.totalPages);
            } catch (err){
                console.log(err);
                setError("Something went wrong");
            } finally {
                setIsLoading(false);
            }
        };

        fetchPosts();
    }, [currentPage]);

    if (isLoading) {
        return <p className="py-10 text-center text-gray-500">Cargando posts...</p>;
    }

    if (error) {
        return <p className="py-10 text-center text-red-500">{error}</p>;
    }

    return (
        <div className="mx-auto max-w-6xl px-4 py-8">
            <h1 className="mb-6 text-3xl font-bold text-gray-900">Blog</h1>
            <PostList posts={posts} />
            {totalPages > 1 && (
                <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={setCurrentPage}
                />
            )}
        </div>
    );
}