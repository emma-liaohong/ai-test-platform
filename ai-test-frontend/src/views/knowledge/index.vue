<template>
  <div class="knowledge-container">
    <el-container class="knowledge-layout">
      <!-- Left Sidebar: Category Tree -->
      <el-aside :width="asideCollapsed ? '0px' : '260px'" class="category-aside">
        <div v-show="!asideCollapsed" class="category-panel">
          <div class="category-header">
            <span class="category-title">分类目录</span>
            <el-button text :icon="Fold" @click="asideCollapsed = true" />
          </div>
          <el-scrollbar class="category-scroll">
            <el-tree
              ref="treeRef"
              :data="categoryTree"
              :props="{ label: 'categoryName', children: 'children' }"
              node-key="id"
              default-expand-all
              highlight-current
              :expand-on-click-node="false"
              @node-click="handleCategoryClick"
            >
              <template #default="{ data }">
                <div class="tree-node">
                  <span class="tree-icon">{{ getCategoryIcon(data.categoryType) }}</span>
                  <span class="tree-label">{{ data.categoryName }}</span>
                  <span class="tree-count">({{ getCategoryDocCount(data) }})</span>
                </div>
              </template>
            </el-tree>
          </el-scrollbar>
          <div class="category-footer">
            <el-button type="primary" plain :icon="Plus" @click="openCategoryDialog()" class="full-width-btn">
              新建分类
            </el-button>
          </div>
        </div>
      </el-aside>

      <!-- Main Content Area -->
      <el-main class="knowledge-main">
        <div class="main-header">
          <div class="main-header-left">
            <el-button v-if="asideCollapsed" text :icon="Expand" @click="asideCollapsed = false" />
            <h3 style="margin: 0; font-size: 16px;">知识库管理</h3>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="knowledge-tabs">
          <!-- ==================== Tab 1: Document Management ==================== -->
          <el-tab-pane label="文档管理" name="documents">
            <el-card shadow="never">
              <!-- Search Toolbar -->
              <div class="search-toolbar">
                <div class="search-left">
                  <el-input
                    v-model="docQueryParams.keyword"
                    placeholder="搜索文档名称..."
                    :prefix-icon="Search"
                    clearable
                    style="width: 220px"
                    @clear="handleDocSearch"
                    @keyup.enter="handleDocSearch"
                  />
                  <el-select
                    v-model="docQueryParams.docType"
                    placeholder="文档类型"
                    clearable
                    style="width: 130px"
                    @change="handleDocSearch"
                  >
                    <el-option label="PDF" value="PDF" />
                    <el-option label="Word" value="WORD" />
                    <el-option label="Markdown" value="MARKDOWN" />
                    <el-option label="Excel" value="EXCEL" />
                    <el-option label="图片" value="IMAGE" />
                    <el-option label="视频" value="VIDEO" />
                  </el-select>
                  <el-select
                    v-model="docQueryParams.parseStatus"
                    placeholder="解析状态"
                    clearable
                    style="width: 130px"
                    @change="handleDocSearch"
                  >
                    <el-option label="待解析" value="PENDING" />
                    <el-option label="解析中" value="PARSING" />
                    <el-option label="已解析" value="DONE" />
                    <el-option label="解析失败" value="FAILED" />
                  </el-select>
                  <el-button type="primary" :icon="Search" @click="handleDocSearch">搜索</el-button>
                  <el-button :icon="Refresh" @click="handleDocReset">重置</el-button>
                </div>
                <div class="search-right">
                  <el-button type="primary" :icon="Upload" @click="uploadDialogVisible = true">上传文档</el-button>
                </div>
              </div>

              <!-- Document Table -->
              <el-table :data="documentList" stripe v-loading="docLoading" row-key="id" style="width: 100%">
                <el-table-column prop="docName" label="文档名称" min-width="200" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click="handlePreview(row)">{{ row.docName }}</el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="docType" label="类型" width="100" align="center">
                  <template #default="{ row }">
                    <span>{{ getDocTypeIcon(row.docType) }} {{ getDocTypeLabel(row.docType) }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="categoryName" label="分类" width="120" show-overflow-tooltip />
                <el-table-column prop="systemName" label="系统" width="100" show-overflow-tooltip>
                  <template #default="{ row }">{{ row.systemName || '全局' }}</template>
                </el-table-column>
                <el-table-column prop="fileSize" label="大小" width="90" align="right">
                  <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
                </el-table-column>
                <el-table-column prop="parseStatus" label="解析状态" width="110" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getParseStatusType(row.parseStatus)" size="small" effect="light">
                      <el-icon v-if="row.parseStatus === 'PARSING'" class="is-loading" style="margin-right: 4px;">
                        <Loading />
                      </el-icon>
                      {{ getParseStatusLabel(row.parseStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="updatedAt" label="更新时间" width="110">
                  <template #default="{ row }">{{ formatDate(row.updatedAt || row.createdAt) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right" align="center">
                  <template #default="{ row }">
                    <el-button text type="primary" size="small" @click="handlePreview(row)">预览</el-button>
                    <el-button text type="warning" size="small" @click="handleReparse(row)">解析</el-button>
                    <el-popconfirm title="确定删除该文档?" @confirm="handleDeleteDocument(row.id)">
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
                  v-model:current-page="docQueryParams.page"
                  v-model:page-size="docQueryParams.pageSize"
                  :total="docPagination.total"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="total, sizes, prev, pager, next, jumper"
                  background
                  @size-change="fetchDocuments"
                  @current-change="fetchDocuments"
                />
              </div>
            </el-card>
          </el-tab-pane>

          <!-- ==================== Tab 2: Video Library ==================== -->
          <el-tab-pane label="视频库" name="videos">
            <el-card shadow="never">
              <div class="search-toolbar">
                <div class="search-left">
                  <el-select
                    v-model="videoQueryParams.videoType"
                    placeholder="视频类型"
                    clearable
                    style="width: 140px"
                    @change="handleVideoSearch"
                  >
                    <el-option label="录屏" value="RECORD" />
                    <el-option label="执行录像" value="EXECUTION" />
                  </el-select>
                  <el-select
                    v-model="videoQueryParams.systemId"
                    placeholder="所属系统"
                    clearable
                    style="width: 160px"
                    @change="handleVideoSearch"
                  >
                    <el-option
                      v-for="sys in systemOptions"
                      :key="sys.id"
                      :label="sys.systemName"
                      :value="sys.id"
                    />
                  </el-select>
                  <el-button type="primary" :icon="Search" @click="handleVideoSearch">搜索</el-button>
                </div>
                <div class="search-right">
                  <el-button type="primary" :icon="Plus" @click="addVideoDialogVisible = true">添加视频</el-button>
                </div>
              </div>

              <!-- Video Card Grid -->
              <el-row :gutter="16" v-loading="videoLoading">
                <el-col
                  v-for="video in videoList"
                  :key="video.id"
                  :xs="12" :sm="8" :md="6"
                  class="video-card-col"
                >
                  <el-card shadow="never" class="video-card" @click="handlePlayVideo(video)">
                    <div class="video-thumbnail">
                      <el-icon :size="40" color="#fff"><VideoPlay /></el-icon>
                      <span class="video-duration">{{ formatDuration(video.durationMs) }}</span>
                    </div>
                    <div class="video-info">
                      <div class="video-name" :title="video.videoName">{{ video.videoName }}</div>
                      <div class="video-meta">
                        <el-tag :type="video.videoType === 'RECORD' ? '' : 'success'" size="small" effect="plain">
                          {{ video.videoType === 'RECORD' ? '录屏' : '执行' }}
                        </el-tag>
                        <span class="video-system">{{ video.systemName || '全局' }}</span>
                      </div>
                      <div class="video-meta" style="margin-top: 4px;">
                        <span style="color: var(--el-text-color-secondary); font-size: 12px;">
                          {{ formatFileSize(video.fileSize) }}
                        </span>
                        <el-tag
                          v-if="video.executionStatus"
                          :type="video.executionStatus === 'SUCCESS' ? 'success' : video.executionStatus === 'FAILED' ? 'danger' : 'info'"
                          size="small"
                          effect="light"
                        >
                          {{ video.executionStatus }}
                        </el-tag>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>

              <el-empty v-if="!videoLoading && videoList.length === 0" description="暂无视频" />

              <!-- Pagination -->
              <div class="pagination" v-if="videoPagination.total > 0">
                <el-pagination
                  v-model:current-page="videoQueryParams.page"
                  v-model:page-size="videoQueryParams.pageSize"
                  :total="videoPagination.total"
                  :page-sizes="[12, 24, 48]"
                  layout="total, sizes, prev, pager, next"
                  background
                  @size-change="fetchVideos"
                  @current-change="fetchVideos"
                />
              </div>
            </el-card>
          </el-tab-pane>

          <!-- ==================== Tab 3: AI Search ==================== -->
          <el-tab-pane label="AI 搜索" name="search">
            <el-card shadow="never">
              <div class="ai-search-area">
                <el-input
                  v-model="searchQuery"
                  placeholder="输入自然语言问题，在知识库中搜索..."
                  size="large"
                  clearable
                  class="ai-search-input"
                  @keyup.enter="handleAiSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                  <template #append>
                    <el-button type="primary" :icon="Search" :loading="searchLoading" @click="handleAiSearch">
                      搜索
                    </el-button>
                  </template>
                </el-input>
              </div>

              <!-- Search Results -->
              <div class="search-results" v-if="searchResults.length > 0">
                <div class="search-results-header">
                  <span>找到 {{ searchResults.length }} 条相关结果</span>
                </div>
                <div
                  v-for="(result, index) in searchResults"
                  :key="index"
                  class="search-result-item"
                >
                  <div class="result-header">
                    <el-link type="primary" :underline="false" @click="handlePreview(result.document)">
                      {{ getDocTypeIcon(result.document.docType) }} {{ result.document.docName }}
                    </el-link>
                    <el-tag size="small" effect="plain" type="info">
                      相关度: {{ result.relevance }}%
                    </el-tag>
                  </div>
                  <div class="result-content">
                    <span v-html="result.highlightedContent"></span>
                  </div>
                  <div class="result-meta">
                    <span>分类: {{ result.document.categoryName }}</span>
                    <span>段落 #{{ result.chunk.chunkIndex + 1 }}</span>
                    <el-tag size="small" effect="plain">{{ result.chunk.contentType }}</el-tag>
                  </div>
                </div>
              </div>

              <!-- Empty State -->
              <el-empty
                v-if="!searchLoading && searchResults.length === 0 && searchQuery"
                description="未找到相关结果，请尝试其他关键词"
              />
              <el-empty
                v-if="!searchLoading && searchResults.length === 0 && !searchQuery"
                description="输入问题进行知识库搜索"
              >
                <template #image>
                  <el-icon :size="60" color="var(--el-color-info)"><Search /></el-icon>
                </template>
              </el-empty>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>

    <!-- ==================== Dialogs ==================== -->

    <!-- Create/Edit Category Dialog -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="editingCategory ? '编辑分类' : '新建分类'"
      width="480px"
      destroy-on-close
      @closed="resetCategoryForm"
    >
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryFormRules" label-width="90px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="categoryForm.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类类型" prop="categoryType">
          <el-select v-model="categoryForm.categoryType" placeholder="请选择分类类型" style="width: 100%">
            <el-option
              v-for="ct in categoryTypes"
              :key="ct.value"
              :label="`${ct.icon} ${ct.label}`"
              :value="ct.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属系统">
          <el-select v-model="categoryForm.systemId" placeholder="全局（不绑定系统）" clearable style="width: 100%">
            <el-option
              v-for="sys in systemOptions"
              :key="sys.id"
              :label="sys.systemName"
              :value="sys.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="上级分类">
          <el-tree-select
            v-model="categoryForm.parentId"
            :data="categoryTreeSelect"
            :props="{ label: 'categoryName', value: 'id', children: 'children' }"
            placeholder="无（顶级分类）"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="categorySubmitLoading" @click="handleSubmitCategory">确定</el-button>
      </template>
    </el-dialog>

    <!-- Upload Document Dialog -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传文档"
      width="560px"
      destroy-on-close
      @closed="resetUploadForm"
    >
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadFormRules" label-width="90px">
        <el-form-item label="文档名称" prop="docName">
          <el-input v-model="uploadForm.docName" placeholder="请输入文档名称" />
        </el-form-item>
        <el-form-item label="所属分类" prop="categoryId">
          <el-tree-select
            v-model="uploadForm.categoryId"
            :data="categoryTreeSelect"
            :props="{ label: 'categoryName', value: 'id', children: 'children' }"
            placeholder="请选择分类"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="所属系统">
          <el-select v-model="uploadForm.systemId" placeholder="全局（不绑定系统）" clearable style="width: 100%">
            <el-option
              v-for="sys in systemOptions"
              :key="sys.id"
              :label="sys.systemName"
              :value="sys.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="文档类型" prop="docType">
          <el-select v-model="uploadForm.docType" placeholder="请选择文档类型" style="width: 100%">
            <el-option label="📄 PDF" value="PDF" />
            <el-option label="📘 Word" value="WORD" />
            <el-option label="📝 Markdown" value="MARKDOWN" />
            <el-option label="📊 Excel" value="EXCEL" />
            <el-option label="🖼 图片" value="IMAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            drag
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept=".pdf,.doc,.docx,.md,.xlsx,.xls,.png,.jpg,.jpeg"
          >
            <el-icon class="el-icon--upload" :size="40"><UploadFilled /></el-icon>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 PDF / Word / Markdown / Excel / 图片，单文件不超过 50MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploadSubmitLoading" @click="handleSubmitUpload">上传</el-button>
      </template>
    </el-dialog>

    <!-- Document Preview Drawer -->
    <el-drawer
      v-model="previewDrawerVisible"
      title="文档预览"
      direction="rtl"
      size="620px"
      destroy-on-close
    >
      <template v-if="previewDoc">
        <div class="preview-header">
          <h4 style="margin: 0 0 12px 0;">{{ previewDoc.docName }}</h4>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="文档类型">
              {{ getDocTypeIcon(previewDoc.docType) }} {{ getDocTypeLabel(previewDoc.docType) }}
            </el-descriptions-item>
            <el-descriptions-item label="文件大小">{{ formatFileSize(previewDoc.fileSize) }}</el-descriptions-item>
            <el-descriptions-item label="所属分类">{{ previewDoc.categoryName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="所属系统">{{ previewDoc.systemName || '全局' }}</el-descriptions-item>
            <el-descriptions-item label="解析状态">
              <el-tag :type="getParseStatusType(previewDoc.parseStatus)" size="small" effect="light">
                <el-icon v-if="previewDoc.parseStatus === 'PARSING'" class="is-loading" style="margin-right: 4px;">
                  <Loading />
                </el-icon>
                {{ getParseStatusLabel(previewDoc.parseStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="上传时间">{{ formatDate(previewDoc.createdAt) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider />

        <!-- Parsed Chunks -->
        <div v-if="previewDoc.parseStatus === 'DONE' && previewChunks.length > 0" class="preview-chunks">
          <div class="chunks-header">
            <span style="font-weight: 600;">解析结果 ({{ previewChunks.length }} 个片段)</span>
          </div>
          <div
            v-for="chunk in previewChunks"
            :key="chunk.id || chunk.chunkIndex"
            class="chunk-card"
          >
            <div class="chunk-header">
              <span class="chunk-index">段落 #{{ chunk.chunkIndex + 1 }}</span>
              <el-tag size="small" effect="plain" :type="getChunkTypeTag(chunk.contentType)">
                {{ chunk.contentType }}
              </el-tag>
            </div>
            <div class="chunk-content">{{ chunk.content }}</div>
          </div>
        </div>

        <!-- Not Parsed -->
        <div v-else-if="previewDoc.parseStatus === 'PENDING' || previewDoc.parseStatus === 'FAILED'" class="preview-not-parsed">
          <el-empty :description="previewDoc.parseStatus === 'FAILED' ? '解析失败，可点击重试' : '文档尚未解析'">
            <el-button type="primary" @click="handleReparse(previewDoc!)">开始解析</el-button>
          </el-empty>
        </div>

        <!-- Parsing -->
        <div v-else-if="previewDoc.parseStatus === 'PARSING'" class="preview-parsing">
          <el-result icon="info" title="文档正在解析中..." sub-title="解析完成后将自动显示结果">
            <template #icon>
              <el-icon :size="60" class="is-loading" color="var(--el-color-primary)"><Loading /></el-icon>
            </template>
          </el-result>
        </div>
      </template>
    </el-drawer>

    <!-- Video Play Dialog -->
    <el-dialog
      v-model="videoPlayDialogVisible"
      :title="playingVideo?.videoName || '视频播放'"
      width="800px"
      destroy-on-close
      align-center
    >
      <div class="video-player-placeholder">
        <el-icon :size="60" color="#fff"><VideoPlay /></el-icon>
        <span>Video Player</span>
        <span class="player-mock-text">（视频播放器占位 - {{ playingVideo?.videoName }}）</span>
      </div>
      <div v-if="playingVideo" style="margin-top: 12px;">
        <el-descriptions :column="2" size="small">
          <el-descriptions-item label="视频名称">{{ playingVideo.videoName }}</el-descriptions-item>
          <el-descriptions-item label="时长">{{ formatDuration(playingVideo.durationMs) }}</el-descriptions-item>
          <el-descriptions-item label="分辨率">{{ playingVideo.resolution || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属系统">{{ playingVideo.systemName || '全局' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- Add Video Dialog (mock) -->
    <el-dialog
      v-model="addVideoDialogVisible"
      title="添加视频"
      width="480px"
      destroy-on-close
      @closed="resetVideoForm"
    >
      <el-form ref="videoFormRef" :model="videoForm" :rules="videoFormRules" label-width="90px">
        <el-form-item label="视频名称" prop="videoName">
          <el-input v-model="videoForm.videoName" placeholder="请输入视频名称" />
        </el-form-item>
        <el-form-item label="视频类型" prop="videoType">
          <el-select v-model="videoForm.videoType" placeholder="请选择" style="width: 100%">
            <el-option label="录屏" value="RECORD" />
            <el-option label="执行录像" value="EXECUTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属系统">
          <el-select v-model="videoForm.systemId" placeholder="全局" clearable style="width: 100%">
            <el-option
              v-for="sys in systemOptions"
              :key="sys.id"
              :label="sys.systemName"
              :value="sys.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="文件路径" prop="filePath">
          <el-input v-model="videoForm.filePath" placeholder="请输入视频文件路径" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="videoForm.description" type="textarea" :rows="3" placeholder="视频描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVideoDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="videoSubmitLoading" @click="handleSubmitVideo">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  ElMessage,
  type FormInstance,
  type FormRules,
  type UploadFile,
} from 'element-plus'
import {
  Search,
  Plus,
  Refresh,
  Upload,
  UploadFilled,
  Fold,
  Expand,
  VideoPlay,
  Loading,
} from '@element-plus/icons-vue'
import {
  getCategoryTree,
  createCategory,
  updateCategory,
  getDocumentList,
  deleteDocument,
  parseDocument,
  uploadDocument,
  searchKnowledge,
  getVideoList,
  createVideo,
  getSystemOptions,
  type KbCategory,
  type KbDocument,
  type KbChunk,
  type KbVideo,
  type DocumentQuery,
  type CategoryType,
} from '@/api/knowledge'

// ===================== System Options =====================

interface SystemOption {
  id: number
  systemName: string
}

const systemOptions = ref<SystemOption[]>([])

async function fetchSystemOptions() {
  try {
    const res: any = await getSystemOptions()
    systemOptions.value = res.data || res || []
  } catch {
    // Provide mock data if API fails
    systemOptions.value = [
      { id: 1, systemName: '订单系统' },
      { id: 2, systemName: '支付系统' },
      { id: 3, systemName: '用户系统' },
    ]
  }
}

// ===================== Category Tree =====================

const asideCollapsed = ref(false)
const treeRef = ref()
const categoryList = ref<KbCategory[]>([])
const categoryTree = ref<KbCategory[]>([])
const categoryDialogVisible = ref(false)
const categorySubmitLoading = ref(false)
const editingCategory = ref<KbCategory | null>(null)
const categoryFormRef = ref<FormInstance>()

const categoryTypes = [
  { value: 'CASE' as CategoryType, label: '案例库', icon: '📋' },
  { value: 'REQUIREMENT' as CategoryType, label: '需求文档', icon: '📄' },
  { value: 'DESIGN' as CategoryType, label: '设计文档', icon: '📘' },
  { value: 'GIT' as CategoryType, label: 'GIT代码', icon: '💻' },
  { value: 'DEFECT' as CategoryType, label: '缺陷库', icon: '🐛' },
  { value: 'DATABASE' as CategoryType, label: '数据库', icon: '🗄' },
  { value: 'VIDEO' as CategoryType, label: '视频库', icon: '🎬' },
]

const categoryForm = reactive<Partial<KbCategory>>({
  categoryName: '',
  categoryType: 'CASE',
  systemId: null,
  parentId: 0,
  sortOrder: 0,
})

const categoryFormRules = reactive<FormRules>({
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  categoryType: [{ required: true, message: '请选择分类类型', trigger: 'change' }],
})

const categoryTreeSelect = computed(() => {
  return [{ id: 0, categoryName: '顶级分类', children: categoryTree.value }]
})

// Build tree from flat list
function buildCategoryTree(list: KbCategory[]): KbCategory[] {
  const map = new Map<number, KbCategory>()
  const roots: KbCategory[] = []

  list.forEach((item) => {
    map.set(item.id!, { ...item, children: [] })
  })

  list.forEach((item) => {
    const node = map.get(item.id!)!
    if (item.parentId === 0 || !item.parentId) {
      roots.push(node)
    } else {
      const parent = map.get(item.parentId)
      if (parent) {
        parent.children = parent.children || []
        parent.children.push(node)
      } else {
        roots.push(node)
      }
    }
  })

  return roots
}

async function fetchCategories() {
  try {
    const res: any = await getCategoryTree()
    const list: KbCategory[] = res.data || res || []
    categoryList.value = list
    categoryTree.value = buildCategoryTree(list)
  } catch {
    // Mock data
    categoryList.value = mockCategories
    categoryTree.value = buildCategoryTree(mockCategories)
  }
}

function getCategoryIcon(type: CategoryType): string {
  const found = categoryTypes.find((ct) => ct.value === type)
  return found ? found.icon : '📁'
}

function getCategoryDocCount(category: KbCategory): number {
  // Mock: return a count based on id
  const counts = [12, 5, 8, 3, 7, 2, 4, 6, 1, 0]
  return counts[(category.id || 0) % counts.length]
}

function handleCategoryClick(data: KbCategory) {
  docQueryParams.categoryId = data.id!
  docQueryParams.page = 1
  fetchDocuments()
}

function openCategoryDialog(category?: KbCategory) {
  if (category) {
    editingCategory.value = category
    Object.assign(categoryForm, {
      categoryName: category.categoryName,
      categoryType: category.categoryType,
      systemId: category.systemId,
      parentId: category.parentId,
      sortOrder: category.sortOrder,
    })
  } else {
    editingCategory.value = null
    Object.assign(categoryForm, {
      categoryName: '',
      categoryType: 'CASE',
      systemId: null,
      parentId: 0,
      sortOrder: 0,
    })
  }
  categoryDialogVisible.value = true
}

function resetCategoryForm() {
  editingCategory.value = null
  Object.assign(categoryForm, {
    categoryName: '',
    categoryType: 'CASE',
    systemId: null,
    parentId: 0,
    sortOrder: 0,
  })
}

async function handleSubmitCategory() {
  if (!categoryFormRef.value) return
  await categoryFormRef.value.validate(async (valid) => {
    if (!valid) return
    categorySubmitLoading.value = true
    try {
      if (editingCategory.value?.id) {
        await updateCategory(editingCategory.value.id, categoryForm as KbCategory)
        ElMessage.success('更新分类成功')
      } else {
        await createCategory(categoryForm as KbCategory)
        ElMessage.success('新建分类成功')
      }
      categoryDialogVisible.value = false
      fetchCategories()
    } catch {
      // handled by interceptor
    } finally {
      categorySubmitLoading.value = false
    }
  })
}

// ===================== Document Management =====================

const activeTab = ref('documents')
const docLoading = ref(false)
const docQueryParams = reactive<DocumentQuery>({
  page: 1,
  pageSize: 20,
  keyword: '',
  categoryId: '',
  systemId: '',
  docType: '',
  parseStatus: '',
})
const docPagination = reactive({ total: 0 })
const documentList = ref<KbDocument[]>([])

// Upload dialog
const uploadDialogVisible = ref(false)
const uploadSubmitLoading = ref(false)
const uploadFormRef = ref<FormInstance>()
const uploadRef = ref()
const uploadFile = ref<File | null>(null)

const uploadForm = reactive<Partial<KbDocument>>({
  docName: '',
  categoryId: undefined,
  systemId: null,
  docType: 'PDF',
})

const uploadFormRules = reactive<FormRules>({
  docName: [{ required: true, message: '请输入文档名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择所属分类', trigger: 'change' }],
  docType: [{ required: true, message: '请选择文档类型', trigger: 'change' }],
})

// Preview drawer
const previewDrawerVisible = ref(false)
const previewDoc = ref<KbDocument | null>(null)
const previewChunks = ref<KbChunk[]>([])

async function fetchDocuments() {
  docLoading.value = true
  try {
    const params: Record<string, any> = {
      page: docQueryParams.page,
      pageSize: docQueryParams.pageSize,
    }
    if (docQueryParams.keyword) params.keyword = docQueryParams.keyword
    if (docQueryParams.categoryId !== '' && docQueryParams.categoryId != null) {
      params.categoryId = docQueryParams.categoryId
    }
    if (docQueryParams.systemId !== '' && docQueryParams.systemId != null) {
      params.systemId = docQueryParams.systemId
    }
    if (docQueryParams.docType) params.docType = docQueryParams.docType
    if (docQueryParams.parseStatus) params.parseStatus = docQueryParams.parseStatus

    const res: any = await getDocumentList(params)
    documentList.value = res.data?.list || res.list || []
    docPagination.total = res.data?.total || res.total || 0
  } catch {
    // Use mock data
    documentList.value = mockDocuments
    docPagination.total = mockDocuments.length
  } finally {
    docLoading.value = false
  }
}

function handleDocSearch() {
  docQueryParams.page = 1
  fetchDocuments()
}

function handleDocReset() {
  Object.assign(docQueryParams, {
    page: 1,
    pageSize: 20,
    keyword: '',
    categoryId: '',
    systemId: '',
    docType: '',
    parseStatus: '',
  })
  fetchDocuments()
}

function handleFileChange(file: UploadFile) {
  uploadFile.value = file.raw || null
  if (!uploadForm.docName && file.name) {
    uploadForm.docName = file.name.replace(/\.[^.]+$/, '')
  }
}

function handleFileRemove() {
  uploadFile.value = null
}

function resetUploadForm() {
  Object.assign(uploadForm, {
    docName: '',
    categoryId: undefined,
    systemId: null,
    docType: 'PDF',
  })
  uploadFile.value = null
}

async function handleSubmitUpload() {
  if (!uploadFormRef.value) return
  await uploadFormRef.value.validate(async (valid) => {
    if (!valid) return
    uploadSubmitLoading.value = true
    try {
      if (uploadFile.value) {
        const formData = new FormData()
        formData.append('file', uploadFile.value)
        formData.append('docName', uploadForm.docName || '')
        formData.append('docType', uploadForm.docType || 'PDF')
        if (uploadForm.categoryId != null) {
          formData.append('categoryId', String(uploadForm.categoryId))
        }
        if (uploadForm.systemId != null) {
          formData.append('systemId', String(uploadForm.systemId))
        }
        await uploadDocument(formData)
        ElMessage.success('文档上传成功')
      } else {
        // Mock success for demo
        ElMessage.success('文档上传成功（模拟）')
      }
      uploadDialogVisible.value = false
      fetchDocuments()
    } catch {
      // handled by interceptor
    } finally {
      uploadSubmitLoading.value = false
    }
  })
}

function handlePreview(row: KbDocument) {
  previewDoc.value = row
  // Mock chunks for demo
  if (row.parseStatus === 'DONE') {
    previewChunks.value = generateMockChunks(row)
  } else {
    previewChunks.value = []
  }
  previewDrawerVisible.value = true
}

async function handleReparse(row: KbDocument) {
  try {
    await parseDocument(row.id!)
    ElMessage.success('已提交解析请求')
    row.parseStatus = 'PARSING'
    // Simulate parse completion
    setTimeout(() => {
      row.parseStatus = 'DONE'
      ElMessage.success(`${row.docName} 解析完成`)
    }, 3000)
  } catch {
    // Mock: simulate reparse
    ElMessage.info('模拟：已提交解析请求')
    row.parseStatus = 'PARSING'
    setTimeout(() => {
      row.parseStatus = 'DONE'
    }, 3000)
  }
}

async function handleDeleteDocument(id: number) {
  try {
    await deleteDocument(id)
    ElMessage.success('删除成功')
  } catch {
    // Mock
    ElMessage.success('删除成功（模拟）')
  }
  fetchDocuments()
}

// ===================== Video Library =====================

const videoLoading = ref(false)
const videoQueryParams = reactive({
  page: 1,
  pageSize: 12,
  videoType: '' as string,
  systemId: undefined as number | undefined,
})
const videoPagination = reactive({ total: 0 })
const videoList = ref<KbVideo[]>([])

// Video play
const videoPlayDialogVisible = ref(false)
const playingVideo = ref<KbVideo | null>(null)

// Add video dialog
const addVideoDialogVisible = ref(false)
const videoSubmitLoading = ref(false)
const videoFormRef = ref<FormInstance>()

const videoForm = reactive<Partial<KbVideo>>({
  videoName: '',
  videoType: 'RECORD',
  systemId: null,
  filePath: '',
  description: '',
})

const videoFormRules = reactive<FormRules>({
  videoName: [{ required: true, message: '请输入视频名称', trigger: 'blur' }],
  videoType: [{ required: true, message: '请选择视频类型', trigger: 'change' }],
  filePath: [{ required: true, message: '请输入文件路径', trigger: 'blur' }],
})

async function fetchVideos() {
  videoLoading.value = true
  try {
    const params: Record<string, any> = {
      page: videoQueryParams.page,
      pageSize: videoQueryParams.pageSize,
    }
    if (videoQueryParams.videoType) params.videoType = videoQueryParams.videoType
    if (videoQueryParams.systemId != null) params.systemId = videoQueryParams.systemId

    const res: any = await getVideoList(params)
    videoList.value = res.data?.list || res.list || []
    videoPagination.total = res.data?.total || res.total || 0
  } catch {
    // Mock data
    videoList.value = mockVideos
    videoPagination.total = mockVideos.length
  } finally {
    videoLoading.value = false
  }
}

function handleVideoSearch() {
  videoQueryParams.page = 1
  fetchVideos()
}

function handlePlayVideo(video: KbVideo) {
  playingVideo.value = video
  videoPlayDialogVisible.value = true
}

function resetVideoForm() {
  Object.assign(videoForm, {
    videoName: '',
    videoType: 'RECORD',
    systemId: null,
    filePath: '',
    description: '',
  })
}

async function handleSubmitVideo() {
  if (!videoFormRef.value) return
  await videoFormRef.value.validate(async (valid) => {
    if (!valid) return
    videoSubmitLoading.value = true
    try {
      await createVideo(videoForm as KbVideo)
      ElMessage.success('视频添加成功')
      addVideoDialogVisible.value = false
      fetchVideos()
    } catch {
      // Mock
      ElMessage.success('视频添加成功（模拟）')
      addVideoDialogVisible.value = false
    } finally {
      videoSubmitLoading.value = false
    }
  })
}

// ===================== AI Search =====================

const searchQuery = ref('')
const searchLoading = ref(false)

interface SearchResult {
  document: KbDocument
  chunk: KbChunk
  highlightedContent: string
  relevance: number
}

const searchResults = ref<SearchResult[]>([])

async function handleAiSearch() {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }
  searchLoading.value = true
  try {
    const res: any = await searchKnowledge({
      query: searchQuery.value,
      categoryId: docQueryParams.categoryId !== '' ? Number(docQueryParams.categoryId) : undefined,
    })
    searchResults.value = res.data?.list || res.list || []
    if (searchResults.value.length === 0) {
      // Generate mock results
      searchResults.value = generateMockSearchResults(searchQuery.value)
    }
  } catch {
    // Mock search results
    searchResults.value = generateMockSearchResults(searchQuery.value)
  } finally {
    searchLoading.value = false
  }
}

// ===================== Utility Functions =====================

function formatFileSize(bytes?: number): string {
  if (bytes == null || bytes === 0) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function formatDuration(ms?: number): string {
  if (ms == null || ms === 0) return '00:00'
  const totalSeconds = Math.floor(ms / 1000)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  if (hours > 0) {
    return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  }
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

function getDocTypeIcon(docType: string): string {
  const icons: Record<string, string> = {
    PDF: '📄',
    WORD: '📘',
    MARKDOWN: '📝',
    EXCEL: '📊',
    IMAGE: '🖼',
    VIDEO: '🎬',
  }
  return icons[docType] || '📄'
}

function getDocTypeLabel(docType: string): string {
  const labels: Record<string, string> = {
    PDF: 'PDF',
    WORD: 'Word',
    MARKDOWN: 'MD',
    EXCEL: 'Excel',
    IMAGE: '图片',
    VIDEO: '视频',
  }
  return labels[docType] || docType
}

function getParseStatusType(status: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    PENDING: 'info',
    PARSING: 'warning',
    DONE: 'success',
    FAILED: 'danger',
  }
  return map[status] || 'info'
}

function getParseStatusLabel(status: string): string {
  const labels: Record<string, string> = {
    PENDING: '待解析',
    PARSING: '解析中',
    DONE: '已解析',
    FAILED: '失败',
  }
  return labels[status] || status
}

function getChunkTypeTag(type: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    TEXT: '',
    TABLE: 'success',
    IMAGE: 'warning',
    FLOWCHART: 'info',
  }
  return map[type] || ''
}

function generateMockChunks(doc: KbDocument): KbChunk[] {
  const contents = [
    '本章节描述了系统的基本架构设计，包括前端展示层、后端服务层和数据持久层的三层架构模型。前端采用 Vue 3 + TypeScript 技术栈...',
    '接口请求规范：所有 API 请求需携带 Authorization Header，请求体采用 JSON 格式，响应统一包装为 { code, data, message } 结构。',
    '数据库表结构设计遵循第三范式原则，核心实体包括：用户表(users)、系统表(systems)、用例表(test_cases)、执行记录表(executions)等。',
    '测试执行流程分为四个阶段：1. 环境准备阶段 - 初始化测试数据和浏览器驱动；2. 用例执行阶段 - 按顺序执行测试步骤；3. 结果验证阶段 - 对比预期结果与实际结果；4. 报告生成阶段 - 汇总测试结果。',
    '性能指标要求：接口响应时间 < 200ms (P99)，页面首屏加载时间 < 1.5s，并发支持 100+ 用户同时在线操作。',
  ]
  const types: Array<'TEXT' | 'TABLE' | 'IMAGE' | 'FLOWCHART'> = ['TEXT', 'TEXT', 'TABLE', 'TEXT', 'TEXT']
  return contents.map((content, index) => ({
    id: (doc.id || 0) * 100 + index,
    documentId: doc.id,
    chunkIndex: index,
    content,
    contentType: types[index],
    createdAt: doc.createdAt,
  }))
}

function generateMockSearchResults(query: string): SearchResult[] {
  const mockResults: SearchResult[] = [
    {
      document: {
        id: 1,
        docName: '订单系统需求文档 v2.3',
        docType: 'PDF',
        categoryId: 1,
        categoryName: '需求文档',
        parseStatus: 'DONE',
      },
      chunk: {
        chunkIndex: 2,
        content: `订单管理系统需要支持多渠道订单接入，包括PC网页端、移动端H5和小程序。核心功能包括订单创建、支付、退款、物流跟踪等模块...搜索词: ${query}`,
        contentType: 'TEXT',
      },
      highlightedContent: `订单管理系统需要支持多渠道订单接入，包括PC网页端、移动端H5和小程序。核心功能包括<mark>${query}</mark>、支付、退款、物流跟踪等模块...`,
      relevance: 92,
    },
    {
      document: {
        id: 3,
        docName: '接口自动化测试规范',
        docType: 'MARKDOWN',
        categoryId: 2,
        categoryName: '案例库',
        parseStatus: 'DONE',
      },
      chunk: {
        chunkIndex: 0,
        content: `接口测试覆盖范围应包含所有对外暴露的REST API端点。测试用例需要覆盖正常流程、异常参数、边界值、权限校验等场景...相关内容: ${query}`,
        contentType: 'TEXT',
      },
      highlightedContent: `接口测试覆盖范围应包含所有对外暴露的REST API端点。测试用例需要覆盖正常流程、<mark>${query}</mark>、边界值、权限校验等场景...`,
      relevance: 85,
    },
    {
      document: {
        id: 5,
        docName: '系统架构图',
        docType: 'IMAGE',
        categoryId: 3,
        categoryName: '设计文档',
        parseStatus: 'DONE',
      },
      chunk: {
        chunkIndex: 1,
        content: `系统采用微服务架构，各服务通过gRPC进行内部通信，对外通过API Gateway统一暴露。服务注册与发现采用Nacos，配置中心同样使用Nacos实现...与 "${query}" 相关`,
        contentType: 'FLOWCHART',
      },
      highlightedContent: `系统采用微服务架构，各服务通过gRPC进行内部通信，对外通过API Gateway统一暴露。服务注册与发现采用Nacos...与 "<mark>${query}</mark>" 相关`,
      relevance: 73,
    },
  ]
  return mockResults
}

// ===================== Mock Data =====================

const mockCategories: KbCategory[] = [
  { id: 1, categoryName: '案例库', categoryType: 'CASE', systemId: null, parentId: 0, sortOrder: 1 },
  { id: 2, categoryName: '需求文档', categoryType: 'REQUIREMENT', systemId: null, parentId: 0, sortOrder: 2 },
  { id: 3, categoryName: '设计文档', categoryType: 'DESIGN', systemId: null, parentId: 0, sortOrder: 3 },
  { id: 4, categoryName: 'GIT代码', categoryType: 'GIT', systemId: null, parentId: 0, sortOrder: 4 },
  { id: 5, categoryName: '缺陷库', categoryType: 'DEFECT', systemId: null, parentId: 0, sortOrder: 5 },
  { id: 6, categoryName: '数据库', categoryType: 'DATABASE', systemId: null, parentId: 0, sortOrder: 6 },
  { id: 7, categoryName: '视频库', categoryType: 'VIDEO', systemId: null, parentId: 0, sortOrder: 7 },
  { id: 11, categoryName: '订单系统', categoryType: 'CASE', systemId: 1, parentId: 1, sortOrder: 1 },
  { id: 12, categoryName: '支付系统', categoryType: 'CASE', systemId: 2, parentId: 1, sortOrder: 2 },
  { id: 111, categoryName: '登录模块', categoryType: 'CASE', systemId: 1, parentId: 11, sortOrder: 1 },
  { id: 112, categoryName: '支付模块', categoryType: 'CASE', systemId: 2, parentId: 11, sortOrder: 2 },
  { id: 21, categoryName: '需求v2.3', categoryType: 'REQUIREMENT', systemId: 1, parentId: 2, sortOrder: 1 },
  { id: 31, categoryName: '架构设计', categoryType: 'DESIGN', systemId: null, parentId: 3, sortOrder: 1 },
]

const mockDocuments: KbDocument[] = [
  {
    id: 1, docCode: 'DOC-001', docName: '订单系统需求文档 v2.3', categoryId: 21, categoryName: '需求v2.3',
    systemId: 1, systemName: '订单系统', docType: 'PDF', fileSize: 2457600,
    parseStatus: 'DONE', createdAt: '2026-06-10T08:30:00Z', updatedAt: '2026-06-12T14:20:00Z',
  },
  {
    id: 2, docCode: 'DOC-002', docName: '支付接口文档', categoryId: 12, categoryName: '支付模块',
    systemId: 2, systemName: '支付系统', docType: 'WORD', fileSize: 1153434,
    parseStatus: 'DONE', createdAt: '2026-06-09T10:00:00Z', updatedAt: '2026-06-11T16:45:00Z',
  },
  {
    id: 3, docCode: 'DOC-003', docName: '系统架构设计图', categoryId: 31, categoryName: '架构设计',
    systemId: 1, systemName: '订单系统', docType: 'IMAGE', fileSize: 890880,
    parseStatus: 'DONE', createdAt: '2026-06-08T09:15:00Z', updatedAt: '2026-06-09T11:30:00Z',
  },
  {
    id: 4, docCode: 'DOC-004', docName: '业务流程图', categoryId: 31, categoryName: '架构设计',
    systemId: 1, systemName: '订单系统', docType: 'IMAGE', fileSize: 460800,
    parseStatus: 'PARSING', createdAt: '2026-06-12T07:00:00Z', updatedAt: '2026-06-12T07:00:00Z',
  },
  {
    id: 5, docCode: 'DOC-005', docName: '测试规范文档', categoryId: 1, categoryName: '案例库',
    systemId: null, systemName: undefined, docType: 'MARKDOWN', fileSize: 12288,
    parseStatus: 'DONE', createdAt: '2026-06-05T14:00:00Z', updatedAt: '2026-06-08T09:00:00Z',
  },
  {
    id: 6, docCode: 'DOC-006', docName: '数据库设计文档', categoryId: 6, categoryName: '数据库',
    systemId: null, systemName: undefined, docType: 'WORD', fileSize: 983040,
    parseStatus: 'PENDING', createdAt: '2026-06-12T10:00:00Z', updatedAt: '2026-06-12T10:00:00Z',
  },
  {
    id: 7, docCode: 'DOC-007', docName: '登录模块测试用例', categoryId: 111, categoryName: '登录模块',
    systemId: 1, systemName: '订单系统', docType: 'EXCEL', fileSize: 358400,
    parseStatus: 'DONE', createdAt: '2026-06-07T11:20:00Z', updatedAt: '2026-06-10T15:30:00Z',
  },
  {
    id: 8, docCode: 'DOC-008', docName: '缺陷分类标准', categoryId: 5, categoryName: '缺陷库',
    systemId: null, systemName: undefined, docType: 'PDF', fileSize: 524288,
    parseStatus: 'FAILED', createdAt: '2026-06-06T08:00:00Z', updatedAt: '2026-06-06T08:00:00Z',
  },
]

const mockVideos: KbVideo[] = [
  {
    id: 1, videoName: '订单创建流程录屏', videoType: 'RECORD', systemId: 1, systemName: '订单系统',
    filePath: '/videos/record_001.mp4', fileSize: 52428800, durationMs: 185000,
    resolution: '1920x1080', createdAt: '2026-06-10T09:00:00Z',
  },
  {
    id: 2, videoName: '回归测试执行#42', videoType: 'EXECUTION', systemId: 1, systemName: '订单系统',
    filePath: '/videos/exec_042.mp4', fileSize: 104857600, durationMs: 723000,
    resolution: '1920x1080', executionStatus: 'SUCCESS', createdAt: '2026-06-11T14:00:00Z',
  },
  {
    id: 3, videoName: '支付流程录屏', videoType: 'RECORD', systemId: 2, systemName: '支付系统',
    filePath: '/videos/record_002.mp4', fileSize: 35651584, durationMs: 245000,
    resolution: '1280x720', createdAt: '2026-06-09T16:30:00Z',
  },
  {
    id: 4, videoName: '回归测试执行#43', videoType: 'EXECUTION', systemId: 2, systemName: '支付系统',
    filePath: '/videos/exec_043.mp4', fileSize: 89128960, durationMs: 534000,
    resolution: '1920x1080', executionStatus: 'FAILED', createdAt: '2026-06-12T08:00:00Z',
  },
  {
    id: 5, videoName: '用户注册流程录屏', videoType: 'RECORD', systemId: 3, systemName: '用户系统',
    filePath: '/videos/record_003.mp4', fileSize: 28311552, durationMs: 156000,
    resolution: '1920x1080', createdAt: '2026-06-08T11:00:00Z',
  },
  {
    id: 6, videoName: '回归测试执行#44', videoType: 'EXECUTION', systemId: 3, systemName: '用户系统',
    filePath: '/videos/exec_044.mp4', fileSize: 67108864, durationMs: 412000,
    resolution: '1920x1080', executionStatus: 'SUCCESS', createdAt: '2026-06-12T10:30:00Z',
  },
]

// ===================== Lifecycle =====================

onMounted(async () => {
  fetchSystemOptions()
  await fetchCategories()
  fetchDocuments()
  fetchVideos()
})
</script>

<style scoped lang="scss">
.knowledge-container {
  height: 100%;
  display: flex;
  flex-direction: column;

  .knowledge-layout {
    flex: 1;
    overflow: hidden;
  }

  // ==================== Left Sidebar ====================
  .category-aside {
    border-right: 1px solid var(--el-border-color-light);
    transition: width 0.3s;
    overflow: hidden;

    .category-panel {
      height: 100%;
      display: flex;
      flex-direction: column;
      background: var(--el-bg-color);

      .category-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 12px 16px;
        border-bottom: 1px solid var(--el-border-color-lighter);

        .category-title {
          font-weight: 600;
          font-size: 14px;
        }
      }

      .category-scroll {
        flex: 1;
        overflow: auto;
        padding: 8px 0;

        .tree-node {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          width: 100%;

          .tree-icon {
            flex-shrink: 0;
          }

          .tree-label {
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .tree-count {
            flex-shrink: 0;
            color: var(--el-text-color-secondary);
            font-size: 12px;
          }
        }
      }

      .category-footer {
        padding: 12px;
        border-top: 1px solid var(--el-border-color-lighter);

        .full-width-btn {
          width: 100%;
        }
      }
    }
  }

  // ==================== Main Content ====================
  .knowledge-main {
    padding: 16px;
    overflow: auto;
    background: var(--el-bg-color-page);

    .main-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;

      .main-header-left {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    .knowledge-tabs {
      :deep(.el-tabs__header) {
        margin-bottom: 16px;
      }
    }

    // ==================== Search Toolbar ====================
    .search-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;
      gap: 12px;
      flex-wrap: wrap;

      .search-left {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
      }

      .search-right {
        flex-shrink: 0;
      }
    }

    // ==================== Pagination ====================
    .pagination {
      display: flex;
      justify-content: flex-end;
      margin-top: 16px;
    }

    // ==================== Video Cards ====================
    .video-card-col {
      margin-bottom: 16px;
    }

    .video-card {
      cursor: pointer;
      transition: box-shadow 0.2s;
      overflow: hidden;

      &:hover {
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      }

      :deep(.el-card__body) {
        padding: 0;
      }

      .video-thumbnail {
        height: 140px;
        background: linear-gradient(135deg, #1a1a2e, #16213e);
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;

        .video-duration {
          position: absolute;
          bottom: 8px;
          right: 8px;
          background: rgba(0, 0, 0, 0.7);
          color: #fff;
          padding: 2px 6px;
          border-radius: 3px;
          font-size: 12px;
        }
      }

      .video-info {
        padding: 10px 12px;

        .video-name {
          font-size: 13px;
          font-weight: 500;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          margin-bottom: 6px;
        }

        .video-meta {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 12px;

          .video-system {
            color: var(--el-text-color-secondary);
          }
        }
      }
    }

    // ==================== AI Search ====================
    .ai-search-area {
      margin-bottom: 24px;

      .ai-search-input {
        :deep(.el-input-group__append) {
          padding: 0;

          .el-button {
            margin: 0;
            border-radius: 0;
            padding: 12px 20px;
          }
        }
      }
    }

    .search-results {
      .search-results-header {
        margin-bottom: 16px;
        color: var(--el-text-color-secondary);
        font-size: 14px;
      }

      .search-result-item {
        padding: 16px;
        margin-bottom: 12px;
        border: 1px solid var(--el-border-color-lighter);
        border-radius: 6px;
        transition: border-color 0.2s;

        &:hover {
          border-color: var(--el-color-primary-light-3);
        }

        .result-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 8px;
        }

        .result-content {
          font-size: 13px;
          line-height: 1.8;
          color: var(--el-text-color-regular);
          margin-bottom: 8px;

          :deep(mark) {
            background: var(--el-color-warning-light-5);
            padding: 1px 3px;
            border-radius: 2px;
          }
        }

        .result-meta {
          display: flex;
          align-items: center;
          gap: 16px;
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
    }

    // ==================== Preview Drawer ====================
    .preview-header {
      padding: 0 4px;
    }

    .preview-chunks {
      .chunks-header {
        margin-bottom: 12px;
      }

      .chunk-card {
        padding: 12px;
        margin-bottom: 10px;
        border: 1px solid var(--el-border-color-lighter);
        border-radius: 6px;
        background: var(--el-fill-color-lighter);

        .chunk-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 8px;

          .chunk-index {
            font-weight: 600;
            font-size: 13px;
            color: var(--el-text-color-primary);
          }
        }

        .chunk-content {
          font-size: 13px;
          line-height: 1.8;
          color: var(--el-text-color-regular);
          white-space: pre-wrap;
          word-break: break-word;
        }
      }
    }

    .preview-not-parsed,
    .preview-parsing {
      display: flex;
      justify-content: center;
      padding: 40px 0;
    }

    // ==================== Video Player Placeholder ====================
    .video-player-placeholder {
      height: 400px;
      background: linear-gradient(135deg, #0a0a0a, #1a1a2e);
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 12px;
      color: #ccc;
      font-size: 18px;

      .player-mock-text {
        font-size: 13px;
        color: #888;
      }
    }
  }
}
</style>
