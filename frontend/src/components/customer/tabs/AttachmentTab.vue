<template>
  <div class="attachment-tab">
    <div class="tab-toolbar">
      <el-upload
        :http-request="handleUpload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        :file-list="fileList"
        multiple
        show-file-list
      >
        <el-button type="primary" size="small">
          <el-icon><Upload /></el-icon> 上传附件
        </el-button>
      </el-upload>
    </div>

    <el-table :data="fileList" stripe size="small">
      <el-table-column prop="name" label="文件名" min-width="200">
        <template #default="{ row }">
          <div class="file-name-cell">
            <el-icon class="file-icon" :class="getFileIconClass(row.name)">
              <Document v-if="isPdf(row.name)" />
              <Picture v-else-if="isImage(row.name)" />
              <Files v-else />
            </el-icon>
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="size" label="大小" width="100">
        <template #default="{ row }">
          {{ formatFileSize(row.size) }}
        </template>
      </el-table-column>
      <el-table-column prop="uploadTime" label="上传时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="isImage(row.name) || isPdf(row.name)" link type="primary" size="small" @click="handlePreview(row)">预览</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDeleteFile(row)">
            <template #reference><el-button link type="danger" size="small">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 图片预览 -->
    <el-dialog v-model="imagePreviewVisible" title="图片预览" width="60%">
      <div class="image-preview">
        <img :src="previewUrl" alt="预览图片" />
      </div>
    </el-dialog>

    <!-- PDF预览 -->
    <el-dialog v-model="pdfPreviewVisible" title="PDF预览" width="80%" height="80%">
      <iframe :src="previewUrl" class="pdf-iframe"></iframe>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Upload, Document, Picture, Files } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { deleteAttachmentV1, getAttachmentListV1, uploadAttachmentV1 } from '@/api/customer'

const props = defineProps({
  customerId: { type: [String, Number], required: true }
})

const fileList = ref([])
const imagePreviewVisible = ref(false)
const pdfPreviewVisible = ref(false)
const previewUrl = ref('')

const loadData = async () => {
  if (!props.customerId) return
  const rows = await getAttachmentListV1(props.customerId)
  fileList.value = rows.map(row => ({
    ...row,
    name: row.fileName,
    size: row.fileSize,
    url: row.fileUrl,
    uploadTime: row.uploadedAt || row.createdTime
  }))
}

const isImage = (name) => /\.(jpg|jpeg|png|gif|webp)$/i.test(name)
const isPdf = (name) => /\.pdf$/i.test(name)

const getFileIconClass = (name) => {
  if (isPdf(name)) return 'file-pdf'
  if (isImage(name)) return 'file-image'
  return 'file-other'
}

const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const handleUpload = async ({ file, onSuccess, onError }) => {
  try {
    const data = await uploadAttachmentV1(props.customerId, {
      attachmentType: 'CUSTOMER_FILE',
      fileName: file.name,
      fileSize: file.size,
      mimeType: file.type,
      fileUrl: '#'
    })
    onSuccess({ data })
  } catch (error) {
    onError(error)
  }
}

const handleUploadSuccess = (res, file) => {
  ElMessage.success('上传成功')
  loadData()
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const beforeUpload = (file) => {
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    ElMessage.warning('文件大小不能超过50MB')
    return false
  }
  return true
}

const handlePreview = (row) => {
  previewUrl.value = row.url || row.response?.data?.url || ''
  if (isImage(row.name)) {
    imagePreviewVisible.value = true
  } else if (isPdf(row.name)) {
    pdfPreviewVisible.value = true
  }
}

const handleDeleteFile = async (row) => {
  await deleteAttachmentV1(props.customerId, row.id)
  await loadData()
  ElMessage.success('删除成功')
}

onMounted(loadData)
watch(() => props.customerId, loadData)
</script>

<style scoped>
.attachment-tab { padding: 8px 0; }
.tab-toolbar { margin-bottom: 16px; }

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  font-size: 20px;
}
.file-pdf { color: #e6a23c; }
.file-image { color: #409eff; }
.file-other { color: #909399; }

.image-preview {
  text-align: center;
}
.image-preview img {
  max-width: 100%;
  max-height: 70vh;
}

.pdf-iframe {
  width: 100%;
  height: 70vh;
  border: none;
}
</style>
