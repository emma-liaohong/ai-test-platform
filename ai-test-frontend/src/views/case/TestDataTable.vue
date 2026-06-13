<template>
  <div class="test-data-table">
    <!-- Column Definition -->
    <div class="columns-section">
      <div class="section-header">
        <span class="section-title">列定义</span>
        <el-button type="primary" size="small" :icon="Plus" :disabled="!editable" @click="addColumn">
          添加列
        </el-button>
      </div>
      <div class="columns-list">
        <div v-for="(col, index) in internalColumns" :key="index" class="column-item">
          <el-input
            v-model="col.label"
            size="small"
            placeholder="列名称"
            style="width: 140px"
            :disabled="!editable"
          />
          <el-input
            v-model="col.key"
            size="small"
            placeholder="字段 key"
            style="width: 140px"
            :disabled="!editable"
          />
          <el-select v-model="col.type" size="small" style="width: 110px" :disabled="!editable">
            <el-option label="字符串" value="string" />
            <el-option label="数字" value="number" />
            <el-option label="布尔" value="boolean" />
            <el-option label="JSON" value="json" />
          </el-select>
          <el-input
            v-model="col.defaultValue"
            size="small"
            placeholder="默认值"
            style="width: 120px"
            :disabled="!editable"
          />
          <el-button
            v-if="editable"
            text
            type="danger"
            size="small"
            :icon="Delete"
            @click="removeColumn(index)"
          />
        </div>
        <el-empty v-if="!internalColumns.length" description="暂无列定义，请添加" :image-size="40" />
      </div>
    </div>

    <!-- Data Rows -->
    <div class="rows-section">
      <div class="section-header">
        <span class="section-title">
          数据行
          <el-tag type="info" size="small" round style="margin-left: 8px">{{ internalRows.length }}</el-tag>
        </span>
        <div class="section-actions">
          <el-button type="primary" size="small" :icon="Plus" :disabled="!editable || !internalColumns.length" @click="addRow">
            添加行
          </el-button>
          <el-button size="small" :disabled="!editable" @click="handleBatchSave">
            <el-icon><Check /></el-icon>保存数据
          </el-button>
        </div>
      </div>

      <el-table
        v-if="internalColumns.length"
        :data="internalRows"
        stripe
        border
        size="small"
        style="width: 100%"
        max-height="400"
      >
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column
          v-for="col in internalColumns"
          :key="col.key"
          :label="col.label"
          min-width="140"
        >
          <template #default="{ row }">
            <template v-if="col.type === 'boolean'">
              <el-switch
                v-model="row.data[col.key]"
                size="small"
                :disabled="!editable"
              />
            </template>
            <template v-else-if="col.type === 'number'">
              <el-input-number
                v-model="row.data[col.key]"
                size="small"
                :disabled="!editable"
                controls-position="right"
                style="width: 100%"
              />
            </template>
            <template v-else>
              <el-input
                v-model="row.data[col.key]"
                size="small"
                :disabled="!editable"
                :placeholder="col.defaultValue || col.label"
              />
            </template>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="70" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" size="small" :disabled="!editable" />
          </template>
        </el-table-column>
        <el-table-column v-if="editable" label="操作" width="80" align="center" fixed="right">
          <template #default="{ $index }">
            <el-button text type="danger" size="small" :icon="Delete" @click="removeRow($index)" />
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="请先定义列" :image-size="50" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Check } from '@element-plus/icons-vue'
import type { DataTable, DataTableColumn, DataTableRow } from '@/api/case'

const props = defineProps<{
  caseId?: number
  dataTable?: DataTable
  editable?: boolean
}>()

const emit = defineEmits<{
  (e: 'update', data: DataTable): void
}>()

const internalColumns = ref<DataTableColumn[]>([])
const internalRows = ref<DataTableRow[]>([])

onMounted(() => {
  if (props.dataTable) {
    internalColumns.value = props.dataTable.columns ? [...props.dataTable.columns] : []
    internalRows.value = props.dataTable.rows
      ? props.dataTable.rows.map((r) => ({ ...r, data: { ...r.data } }))
      : []
  }
})

// Watch for external changes
watch(
  () => props.dataTable,
  (newVal) => {
    if (newVal) {
      internalColumns.value = newVal.columns ? [...newVal.columns] : []
      internalRows.value = newVal.rows
        ? newVal.rows.map((r) => ({ ...r, data: { ...r.data } }))
        : []
    }
  },
  { deep: true }
)

function addColumn() {
  internalColumns.value.push({
    key: `field_${internalColumns.value.length + 1}`,
    label: `字段${internalColumns.value.length + 1}`,
    type: 'string',
    defaultValue: '',
  })
  // Add empty value for this column in existing rows
  internalRows.value.forEach((row) => {
    row.data[`field_${internalColumns.value.length}`] = ''
  })
  emitUpdate()
}

function removeColumn(index: number) {
  const col = internalColumns.value[index]
  internalColumns.value.splice(index, 1)
  // Remove data for this column from all rows
  internalRows.value.forEach((row) => {
    delete row.data[col.key]
  })
  emitUpdate()
}

function addRow() {
  const rowData: Record<string, any> = {}
  internalColumns.value.forEach((col) => {
    if (col.type === 'boolean') {
      rowData[col.key] = false
    } else if (col.type === 'number') {
      rowData[col.key] = col.defaultValue ? Number(col.defaultValue) : 0
    } else {
      rowData[col.key] = col.defaultValue || ''
    }
  })

  const newRow: DataTableRow = {
    rowOrder: internalRows.value.length + 1,
    data: rowData,
    enabled: true,
  }
  internalRows.value.push(newRow)
  emitUpdate()
}

function removeRow(index: number) {
  internalRows.value.splice(index, 1)
  // Reorder
  internalRows.value.forEach((row, i) => {
    row.rowOrder = i + 1
  })
  emitUpdate()
}

function handleBatchSave() {
  emitUpdate()
  ElMessage.success('测试数据已保存')
}

function emitUpdate() {
  const dataTable: DataTable = {
    caseId: props.caseId,
    name: '测试数据表',
    columns: [...internalColumns.value],
    rows: internalRows.value.map((r) => ({ ...r, data: { ...r.data } })),
  }
  emit('update', dataTable)
}
</script>

<style scoped lang="scss">
.test-data-table {
  .columns-section {
    margin-bottom: 20px;
  }

  .rows-section {
    margin-top: 16px;
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .section-actions {
      display: flex;
      gap: 8px;
    }
  }

  .columns-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 12px;
    background: var(--el-fill-color-lighter);
    border-radius: 4px;
    border: 1px solid var(--el-border-color-lighter);
  }

  .column-item {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}
</style>
