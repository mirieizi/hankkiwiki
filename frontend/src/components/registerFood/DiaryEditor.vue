<template>
  <div class="diary-container">
    <div class="diary-header">
      <p class="date">{{ formattedDate }}</p>
      <div class="user">
        <img src="@/assets/logo.png" alt="프로필" class="profile-image" />
        <span>{{ userName }} 님</span>
      </div>
    </div>

    <p class="label">아래에 일기를 작성해주세요 (최대 200자)</p>

    <div class="diary-input-section">
      <textarea
        v-model="diaryContent"
        class="diary-textarea"
        placeholder="오늘 하루는 어땠나요?&#10;먹은 음식은 어땠나요?&#10;기분이나 느낀 점을 자유롭게 적어보세요!"
        :disabled="loading"
        maxlength="200"
      ></textarea>

      <div class="character-count" :class="{ 'over-limit': diaryContent.length > 200 }">{{ diaryContent.length }} / 200자</div>
    </div>

    <div class="button-group">
      <button class="load-button" @click="loadExistingDiary" :disabled="loading" v-if="!hasLoadedOnce">기존 일기 불러오기</button>

      <button class="submit-button" @click="submitDiary" :disabled="loading || !diaryContent.trim() || diaryContent.length > 200">
        {{ loading ? "처리 중..." : isEditMode ? "일기 수정하기" : "일기 등록하기" }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { format, parseISO } from "date-fns";
import { ko } from "date-fns/locale";
import { toast } from "vue3-toastify"; // ✅ 토스트 import 추가
import { diaryService } from "@/services/diaryService";

const props = defineProps({
  date: { type: String, required: true },
  userName: { type: String, required: true },
  isEditMode: { type: Boolean, default: false }, // ✅ 수정 모드 prop 추가
});

const emit = defineEmits(["diary-saved"]);

const diaryContent = ref("");
const loading = ref(false);
const isEditMode = ref(false);
const hasLoadedOnce = ref(false);
const currentDiaryId = ref(null);
const router = useRouter();

// 포맷된 날짜
const formattedDate = computed(() => {
  try {
    const dateObj = parseISO(props.date);
    return format(dateObj, "yyyy년 MM월 dd일 EEEE", { locale: ko });
  } catch (error) {
    return props.date;
  }
});

// 컴포넌트 마운트 시 기존 일기 자동 로드
onMounted(async () => {
  await loadExistingDiary();
});

// 날짜가 변경될 때마다 일기 다시 로드
watch(
  () => props.date,
  async () => {
    hasLoadedOnce.value = false;
    isEditMode.value = false;
    currentDiaryId.value = null;
    diaryContent.value = "";
    await loadExistingDiary();
  }
);

// 기존 일기 로드 (DiaryResponse 구조 기준)
async function loadExistingDiary() {
  if (hasLoadedOnce.value) return;

  loading.value = true;

  try {
    const diary = await diaryService.getDiaryByDate(props.date);

    if (diary && diary.content) {
      // DiaryResponse: { id, date, content }
      diaryContent.value = diary.content;
      currentDiaryId.value = diary.id;
      isEditMode.value = true;
      console.log("기존 일기 로드됨:", diary);
    } else {
      diaryContent.value = "";
      currentDiaryId.value = null;
      isEditMode.value = false;
      console.log("해당 날짜에 일기 없음");
    }
  } catch (error) {
    if (error.response?.status === 404) {
      // 일기가 없는 경우는 정상적인 상황
      diaryContent.value = "";
      isEditMode.value = false;
      currentDiaryId.value = null;
    } else if (error.response?.status === 401) {
      // ✅ alert → toast로 변경
      toast.error("로그인이 필요합니다. 🔐");
      setTimeout(() => router.push("/login"), 2000);
      return;
    } else {
      console.error("기존 일기 로드 실패:", error);
      // ✅ alert → toast로 변경
      toast.error("일기를 불러오는 중 오류가 발생했습니다. 😢");
    }
  } finally {
    loading.value = false;
    hasLoadedOnce.value = true;
  }
}

// 일기 등록/수정
async function submitDiary() {
  // 유효성 검사
  if (!diaryContent.value.trim()) {
    // ✅ alert → toast로 변경
    toast.warning("일기 내용을 입력해주세요. ✏️");
    return;
  }

  if (diaryContent.value.length > 200) {
    // ✅ alert → toast로 변경
    toast.warning("일기는 200자 이내로 작성해주세요. 📝");
    return;
  }

  const action = isEditMode.value ? "수정" : "등록";

  // ✅ confirm → toast로 변경 (바로 저장)
  toast.info(`일기를 ${action}합니다... 💾`);

  loading.value = true;

  try {
    if (isEditMode.value && currentDiaryId.value) {
      // 수정 (DiaryUpdateRequest)
      const result = await diaryService.updateDiary(currentDiaryId.value, {
        content: diaryContent.value,
        date: props.date,
      });
      // ✅ 성공 토스트
      toast.success("일기가 성공적으로 수정되었습니다! 🎉");
      console.log("일기 수정 완료:", result);
    } else {
      // 새로 생성 (DiaryCreateRequest)
      const result = await diaryService.createDiary({
        content: diaryContent.value,
        date: props.date,
      });

      currentDiaryId.value = result; // Long 타입의 ID
      isEditMode.value = true;
      // ✅ 성공 토스트
      toast.success("일기가 성공적으로 등록되었습니다! 🎉");
      console.log("일기 생성 완료, ID:", result);
    }

    emit("diary-saved");
  } catch (error) {
    console.error("일기 저장 실패:", error);

    // ✅ 상세한 에러 처리 (토스트 적용)
    if (error.message && error.message.includes("200자")) {
      toast.error(error.message);
    } else if (error.response?.status === 401) {
      toast.error("로그인이 필요합니다. 🔐");
      setTimeout(() => router.push("/login"), 2000);
    } else if (error.response?.status === 400) {
      toast.error(error.response.data?.message || "입력값을 확인해주세요. ⚠️");
    } else if (error.response?.status >= 500) {
      toast.error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요. 🛠️");
    } else {
      toast.error(`일기 ${action} 중 오류가 발생했습니다. 😢`);
    }
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.diary-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 100%;
  width: 100%;
}

.diary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 1rem;
  border-bottom: 2px solid rgba(33, 213, 155, 0.2); /* ✅ 민트색으로 변경 */
}

.date {
  font-weight: 700;
  font-size: 1.3rem;
  color: #2d5a52;
  margin: 0;
}

.user {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #2d5a52;
  font-weight: 600;
}

.profile-image {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #21d59b; /* ✅ 민트색으로 변경 */
  background: white;
  padding: 2px;
}

.label {
  margin-bottom: 0.5rem;
  font-size: 1rem;
  color: #2d5a52;
  font-weight: 600;
}

.diary-input-section {
  position: relative;
}

.diary-textarea {
  width: 100%;
  min-height: 300px;
  padding: 1.5rem;
  border: 2px solid rgba(33, 213, 155, 0.2); /* ✅ 민트색으로 변경 */
  border-radius: 12px;
  resize: vertical;
  font-size: 1rem;
  font-family: inherit;
  line-height: 1.6;
  transition: all 0.2s ease;
  box-sizing: border-box;
  background: rgba(248, 255, 252, 0.8); /* ✅ 연한 민트색 배경 */
}

.diary-textarea:focus {
  outline: none;
  border-color: #21d59b; /* ✅ 민트색 포커스 */
  background: white;
  box-shadow: 0 0 0 3px rgba(33, 213, 155, 0.1);
}

.diary-textarea:disabled {
  background-color: rgba(245, 245, 245, 0.8);
  cursor: not-allowed;
}

.character-count {
  text-align: right;
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: #6b7280;
  transition: color 0.2s ease;
}

.character-count.over-limit {
  color: #dc2626;
  font-weight: 600;
}

.button-group {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.load-button {
  background: linear-gradient(90deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  padding: 1rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 1;
  min-width: 150px;
  font-size: 1rem;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
}

.load-button:hover:not(:disabled) {
  background: linear-gradient(90deg, #2563eb 0%, #1d4ed8 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.submit-button {
  background: linear-gradient(90deg, #21d59b 0%, #1bc489 100%); /* ✅ 민트색 그라데이션 */
  color: white;
  border: none;
  padding: 1rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 2;
  min-width: 200px;
  font-size: 1rem;
  box-shadow: 0 4px 15px rgba(33, 213, 155, 0.3);
}

.submit-button:hover:not(:disabled) {
  background: linear-gradient(90deg, #1bc489 0%, #17a673 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(33, 213, 155, 0.4);
}

.load-button:disabled,
.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

@media screen and (max-width: 640px) {
  .button-group {
    flex-direction: column;
  }

  .load-button,
  .submit-button {
    flex: 1;
    min-width: unset;
  }

  .diary-textarea {
    min-height: 250px;
    padding: 1rem;
  }

  .diary-header {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
  }
}
</style>
