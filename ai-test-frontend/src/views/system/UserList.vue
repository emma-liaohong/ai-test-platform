<template>
  <div class="user-list-container">
    <el-card shadow="never">
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名/昵称"
            :prefix-icon="Search"
            clearable
            style="width: 280px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 140px" @change="handleSearch">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </div>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增用户</el-button>
      </div>

      <!-- Table -->
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="roles" label="角色" width="140">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" class="role-tag">{{ role }}</el-tag>
          </template>
        </el-table-column>
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
            <el-button text type="warning" size="small" @click="handleResetPassword(row)">重置密码</el-button>
            <el-popconfirm title="确定删除该用户吗?" @confirm="handleDelete(row.id)">
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
          v-model:page-size="pagination.size"
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
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="formData.roles" multiple placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="测试工程师" value="tester" />
            <el-option label="开发工程师" value="developer" />
            <el-option label="产品经理" value="pm" />
          </el-select>
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
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getUserList, createUser, updateUser, deleteUser, resetUserPassword, type User } from '@/api/user'

const loading = ref(false)
const submitLoading = ref(false)
const searchKeyword = ref('')
const filterStatus = ref<number | undefined>(undefined)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const tableData = ref<User[]>([])

const formData = reactive<User>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  roles: [],
  status: 1,
})

const formRules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
})

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getUserList({
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value,
      status: filterStatus.value,
    })
    tableData.value = res.data?.records || res.data?.list || []
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

function openDialog(row?: User) {
  isEdit.value = !!row
  if (row) {
    Object.assign(formData, row)
  } else {
    Object.assign(formData, { username: '', password: '', nickname: '', email: '', phone: '', roles: [], status: 1 })
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
        await updateUser(formData.id, formData)
        ElMessage.success('更新成功')
      } else {
        await createUser(formData)
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
    await deleteUser(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // Error handled by interceptor
  }
}

async function handleResetPassword(row: User) {
  const { value } = await ElMessageBox.prompt(`请输入用户 "${row.username}" 的新密码`, '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.{6,}/,
    inputErrorMessage: '密码长度不能少于6位',
  })
  try {
    await resetUserPassword(row.id!, value)
    ElMessage.success('密码重置成功')
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

  .toolbar-left {
    display: flex;
    gap: 12px;
  }
}

.role-tag {
  margin-right: 4px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
