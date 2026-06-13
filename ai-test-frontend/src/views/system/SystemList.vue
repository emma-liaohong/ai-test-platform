<template>
  <div class="system-list-container">
    <el-card shadow="never">
      <!-- Toolbar -->
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索系统名称/编码"
          :prefix-icon="Search"
          clearable
          style="width: 280px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增系统</el-button>
      </div>

      <!-- Table -->
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="系统名称" min-width="150" />
        <el-table-column prop="code" label="系统编码" width="150" />
        <el-table-column prop="baseUrl" label="基础URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该系统吗?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑系统' : '新增系统'"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="系统名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入系统名称" />
        </el-form-item>
        <el-form-item label="系统编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入系统编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="基础URL" prop="baseUrl">
          <el-input v-model="formData.baseUrl" placeholder="请输入基础URL，如 http://example.com" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getSystemList, createSystem, updateSystem, deleteSystem, type System } from '@/api/system'

const loading = ref(false)
const submitLoading = ref(false)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const tableData = ref<System[]>([])

const formData = reactive<System>({
  name: '',
  code: '',
  baseUrl: '',
  description: '',
  status: 1,
})

const formRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入系统名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入系统编码', trigger: 'blur' }],
  baseUrl: [{ required: true, message: '请输入基础URL', trigger: 'blur' }],
})

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getSystemList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value,
    })
    tableData.value = res.data?.list || res.list || []
    pagination.total = res.data?.total || res.total || 0
  } catch {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function openDialog(row?: System) {
  isEdit.value = !!row
  if (row) {
    Object.assign(formData, row)
  } else {
    Object.assign(formData, { name: '', code: '', baseUrl: '', description: '', status: 1 })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      if (isEdit.value && formData.id) {
        await updateSystem(formData.id, formData)
        ElMessage.success('更新成功')
      } else {
        await createSystem(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch {
      // Error handled by interceptor
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleDelete(id: number) {
  try {
    await deleteSystem(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // Error handled by interceptor
  }
}
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
