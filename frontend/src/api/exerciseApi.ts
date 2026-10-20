import http from "./http";
import type {
  ExerciseCategoryResponse,
  ExerciseCreateRequest,
  ExerciseResponse,
  ExerciseTypeResponse,
  ExerciseUpdateRequest,
} from "../types/exercise";
import { requireUserId } from "../stores/auth";

export async function getExercises(
  exerciseTypeId?: number,
  userId = requireUserId(),
  includeDisabled = false,
): Promise<ExerciseResponse[]> {
  const response = await http.get<ExerciseResponse[]>(
    "/exercises",
    { params: { exerciseTypeId, userId, includeDisabled } },
  );
  return response.data;
}

export async function getExerciseCategories(includeDisabled = false): Promise<
  ExerciseCategoryResponse[]
> {
  const response = await http.get<
    ExerciseCategoryResponse[]
  >("/exercise-categories", { params: { includeDisabled } });
  return response.data;
}

export async function getExerciseTypes(
  categoryId: number,
  includeDisabled = false,
): Promise<ExerciseTypeResponse[]> {
  const response = await http.get<ExerciseTypeResponse[]>(
    "/exercise-types",
    { params: { categoryId, includeDisabled } },
  );
  return response.data;
}

export async function getExercise(
  id: number,
): Promise<ExerciseResponse> {
  const response = await http.get<ExerciseResponse>(
    `/exercises/${id}`,
  );
  return response.data;
}

export async function createExercise(
  request: ExerciseCreateRequest,
): Promise<ExerciseResponse> {
  const response = await http.post<ExerciseResponse>(
    "/exercises",
    request,
  );
  return response.data;
}

export async function updateExercise(
  id: number,
  request: ExerciseUpdateRequest,
): Promise<ExerciseResponse> {
  const response = await http.put<ExerciseResponse>(
    `/exercises/${id}`,
    request,
  );
  return response.data;
}
