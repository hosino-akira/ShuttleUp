export type MasterKind = 'CATEGORY' | 'TYPE' | 'EXERCISE'
export type TrainingKind = 'PHYSICAL' | 'SKILL' | 'OTHER'

export interface MasterItem {
  id: number
  parentId: number | null
  name: string
  enabled: boolean
  available: boolean
  systemPreset: boolean
  statisticsType: TrainingKind | null
}

export interface TrainingMaster {
  categories: MasterItem[]
  types: MasterItem[]
  exercises: MasterItem[]
}

export interface MasterCreateRequest {
  name: string
  parentId?: number
  statisticsType?: TrainingKind
}
