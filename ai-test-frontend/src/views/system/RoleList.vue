<template>
  <div class="role-list-container">
    <el-card shadow="never">
      <!-- Toolbar -->
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索角色名称"
          :prefix-icon="Search"
          clearable
          style="width: 280px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增角色</el-button>
      </div>

      <!-- Table -->
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="160" />
        <el-table-column prop="code" label="角色编码" width="160" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userCount" label="用户数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button text type="warning" size="small" @click="handlePermission(row)">权限配置</el-button>
            <el-popconfirm title="确定删除该角色吗?" @confirm="handleDelete(row.id)">
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

    <!-- Role Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入角色编码" :disabled="isEdit" />
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

    <!-- Permission Dialog -->
    <el-dialog
      v-model="permDialogVisible"
      title="权限配置"
      width="500px"
      destroy-on-close
    >
      <p style="color: var(--el-text-color-secondary); margin-bottom: 16px;">
        为角色 <strong>{{ currentRole?.name }}</strong> 配置权限
      </p>
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        :default-checked-keys="currentRole?.permissions || []"
        :props="{ label: 'label', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'

interface Role {
  id: number
  name: string
  code: string
  description?: string
  userCount?: number
  status?: number
  permissions?: number[]
  createdAt?: string
}

const loading = ref(false)
const submitLoading = ref(false)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const permDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const treeRef = ref()
const currentRole = ref<Role | null>(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const tableData = ref<Role[]>([])

const formData = reactive({
  name: '',
  code: '',
  description: '',
  status: 1,
})

const formRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
})

// Mock permission tree
const permissionTree = ref([
  {
    id: 1,
    label: '测试管理',
    children: [
      { id: 11, label: '查看用例' },
      { id: 12, label: '创建用例' },
      { id: 13, label: '编辑用例' },
      { id: 14, label: '删除用例' },
      { id: 15, label: '执行用例' },
    ],
  },
  {
    id: 2,
    label: '缺陷管理',
    children: [
      { id: 21, label: '查看缺陷' },
      { id: 22, label: '创建缺陷' },
      { id: 23, label: '编辑缺陷' },
    ],
  },
  {
    id: 3,
    label: '系统管理',
    children: [
      { id: 31, label: '用户管理' },
      { id: 32, label: '角色管理' },
      { id: 33, label: '系统设置' },
    ],
  },
])

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    // Mock data for now
    await new Promise((resolve) => setTimeout(resolve, 300))
    tableData.value = [
      { id: 1, name: '超级管理员', code: 'admin', description: '拥有所有权限', userCount: 2, status: 1, createdAt: '2026-01-01 00:00:00' },
      { id: 2, name: '测试工程师', code: 'tester', description: '测试相关权限', userCount: 5, status: 1, createdAt: '2026-01-01 00:00:00' },
      { id: 3, name: '开发工程师', code: 'developer', description: '开发和查看权限', userCount: 8, status: 1, createdAt: '2026-01-15 00:00:00' },
      { id: 4, name: '产品经理', code: 'pm', description: '只读权限', userCount: 3, status: 1, createdAt: '2026-02-01 00:00:00' },
    ]
    pagination.total = 4
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function openDialog(row?: Role) {
  isEdit.value = !!row
  if (row) {
    Object.assign(formData, row)
  } else {
    Object.assign(formData, { name: '', code: '', description: '', status: 1 })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchData()
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleDelete(id: number) {
  ElMessage.success('删除成功')
  fetchData()
}

function handlePermission(row: Role) {
  currentRole.value = row
  permDialogVisible.value = true
}

function handleSavePermission() {
  ElMessage.success('权限保存成功')
  permDialogVisible.value = false
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
