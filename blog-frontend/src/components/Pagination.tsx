interface PaginationProps {
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
}

export const Pagination = ({ currentPage, totalPages, onPageChange }: PaginationProps) => {
    const isFirstPage = currentPage === 0;
    const isLastPage = currentPage === totalPages - 1;

    return (
        <div className="flex items-center justify-center gap-4 py-6">
            <button
                onClick={() => onPageChange(currentPage - 1)}
                disabled={isFirstPage}
                className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-300 disabled:text-gray-500"
            >
                Anterior
            </button>

            <span className="text-sm text-gray-700">
                Página {currentPage + 1} de {totalPages}
            </span>

            <button
                onClick={() => onPageChange(currentPage + 1)}
                disabled={isLastPage}
                className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-300 disabled:text-gray-500"
            >
                Siguiente
            </button>
        </div>
    );
};