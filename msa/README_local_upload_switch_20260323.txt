S3 제거 버전 적용 안내

변경 내용
1. 상품 이미지를 S3가 아니라 product 파드 내부 로컬 폴더(/app/uploads)에 저장하도록 변경
2. DB에는 /product/images/{파일명} 형식으로 저장
3. product-service가 /product/images/{fileName} 로 이미지를 직접 반환
4. product deployment에서 aws-secret 참조 제거
5. APP_UPLOAD_DIR=/app/uploads 환경변수 추가

적용 순서
1. 이 압축본으로 기존 msa 폴더 교체
2. product 서비스만 다시 빌드/푸시
   cd msa/product
   docker build -t 683668078297.dkr.ecr.ap-northeast-2.amazonaws.com/product-service:latest .
   docker push 683668078297.dkr.ecr.ap-northeast-2.amazonaws.com/product-service:latest
3. 쿠버네티스 재배포
   kubectl apply -f msa/product/k8s/depl_svc.yml -n soldesk
   kubectl rollout restart deployment/product-depl -n soldesk
4. 확인
   kubectl get pods -n soldesk
   kubectl logs -f deployment/product-depl -n soldesk

주의
- 로컬 업로드 파일은 product 파드 내부에 저장되므로 파드가 삭제/재생성되면 이미지 파일이 사라질 수 있음
- 지금 목적은 기능 우선 완성이므로 임시로 로컬 업로드 구조를 사용
