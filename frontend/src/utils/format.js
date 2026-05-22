export const getCoverImage = (images) => {
  if (!images) return 'https://via.placeholder.com/200'
  try {
    return JSON.parse(images)[0]
  } catch (e) {
    return images.split(',')[0] // Fallback
  }
}

export const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

export const getStatusText = (orderOrStatus) => {
  let status, paymentMethod;
  if (orderOrStatus !== null && typeof orderOrStatus === 'object') {
    status = orderOrStatus.status;
    paymentMethod = orderOrStatus.paymentMethod;
  } else {
    status = orderOrStatus;
  }

  if (status === 0) {
    return paymentMethod === 2 ? '待确认' : '待支付';
  }

  const statusMap = {
    1: '进行中',
    2: '已完成',
    3: '已取消'
  }
  return statusMap[status] || '未知状态'
}