-- PostgreSQL enforcement of active hold checks. These are demo database guardrails,
-- NOT pharmacist certification, payment processing, or a complete production API.
BEGIN;

CREATE OR REPLACE FUNCTION pharmasiu_check_hold() RETURNS trigger AS $$
DECLARE
  selected_lot RECORD;
  selected_order RECORD;
  reserved BIGINT;
  approver_allowed BOOLEAN;
BEGIN
  IF NEW.so_luong_giu=0 THEN RETURN NEW; END IF;

  -- Lock selected lot so two concurrent reservations cannot both see free units.
  SELECT l.so_luong_ton,l.han_su_dung,l.tinh_trang,p.loai_thuoc INTO selected_lot
  FROM lo_thuoc l JOIN san_pham p ON p.san_pham_id=l.san_pham_id
  WHERE l.lo_thuoc_id=NEW.lo_thuoc_id AND l.san_pham_id=NEW.san_pham_id
  FOR UPDATE OF l;
  IF NOT FOUND OR selected_lot.tinh_trang<>'DAT' OR selected_lot.han_su_dung<=CURRENT_DATE
  THEN RAISE EXCEPTION 'INVALID_HOLD: lot missing, quarantined or expired'; END IF;

  SELECT d.trang_thai,d.het_han_giu_luc,d.trang_thai_toa,d.nguoi_duyet_toa_id
  INTO selected_order FROM don_hang d WHERE d.don_hang_id=NEW.don_hang_id;
  IF NOT FOUND OR selected_order.trang_thai NOT IN ('DA_GIU_HANG','SAN_SANG_NHAN')
       OR selected_order.het_han_giu_luc IS NULL
       OR selected_order.het_han_giu_luc<=CURRENT_TIMESTAMP
  THEN RAISE EXCEPTION 'INVALID_HOLD: order status or hold expiry'; END IF;

  IF selected_lot.loai_thuoc='RX' THEN
    SELECT COALESCE(a.co_quyen_duyet_toa,FALSE) INTO approver_allowed
    FROM tai_khoan a WHERE a.tai_khoan_id=selected_order.nguoi_duyet_toa_id;
    IF selected_order.trang_thai_toa<>'DA_DUYET' OR NOT COALESCE(approver_allowed,FALSE)
    THEN RAISE EXCEPTION 'RX_HOLD_REQUIRES_AUTHORIZED_APPROVAL'; END IF;
  END IF;

  IF TG_OP='UPDATE' THEN
    SELECT COALESCE(SUM(c.so_luong_giu),0) INTO reserved
    FROM chi_tiet_don_hang c JOIN don_hang d ON d.don_hang_id=c.don_hang_id
    WHERE c.lo_thuoc_id=NEW.lo_thuoc_id AND c.chi_tiet_id<>OLD.chi_tiet_id
      AND d.trang_thai IN ('DA_GIU_HANG','SAN_SANG_NHAN')
      AND d.het_han_giu_luc>CURRENT_TIMESTAMP;
  ELSE
    SELECT COALESCE(SUM(c.so_luong_giu),0) INTO reserved
    FROM chi_tiet_don_hang c JOIN don_hang d ON d.don_hang_id=c.don_hang_id
    WHERE c.lo_thuoc_id=NEW.lo_thuoc_id
      AND d.trang_thai IN ('DA_GIU_HANG','SAN_SANG_NHAN')
      AND d.het_han_giu_luc>CURRENT_TIMESTAMP;
  END IF;
  IF NEW.so_luong_giu+reserved > selected_lot.so_luong_ton
  THEN RAISE EXCEPTION 'INSUFFICIENT_AVAILABLE_LOT_STOCK'; END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_hold_guard
  BEFORE INSERT OR UPDATE OF so_luong_giu,lo_thuoc_id,san_pham_id,don_hang_id
  ON chi_tiet_don_hang FOR EACH ROW EXECUTE FUNCTION pharmasiu_check_hold();

CREATE OR REPLACE FUNCTION pharmasiu_release_before_finish() RETURNS trigger AS $$
BEGIN
  IF NEW.trang_thai NOT IN ('DA_GIU_HANG','SAN_SANG_NHAN')
    AND EXISTS(SELECT 1 FROM chi_tiet_don_hang c
               WHERE c.don_hang_id=OLD.don_hang_id AND c.so_luong_giu>0)
  THEN RAISE EXCEPTION 'RELEASE_HOLD_FIRST'; END IF;
  IF NEW.trang_thai<>'HOAN_TAT'
    AND EXISTS(SELECT 1 FROM chi_tiet_don_hang c
               WHERE c.don_hang_id=OLD.don_hang_id AND c.so_luong_xuat>0)
  THEN RAISE EXCEPTION 'ISSUED_LINES: cannot revert issued order'; END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_release_before_finish
  BEFORE UPDATE OF trang_thai ON don_hang FOR EACH ROW
  EXECUTE FUNCTION pharmasiu_release_before_finish();

CREATE OR REPLACE FUNCTION pharmasiu_guard_lot_stock() RETURNS trigger AS $$
DECLARE
 active_hold BIGINT;
BEGIN
 SELECT COALESCE(SUM(c.so_luong_giu),0) INTO active_hold
 FROM chi_tiet_don_hang c JOIN don_hang d ON d.don_hang_id=c.don_hang_id
 WHERE c.lo_thuoc_id=OLD.lo_thuoc_id
   AND d.trang_thai IN ('DA_GIU_HANG','SAN_SANG_NHAN')
   AND d.het_han_giu_luc>CURRENT_TIMESTAMP;
 IF active_hold>0 AND
    (NEW.so_luong_ton<active_hold OR NEW.tinh_trang<>'DAT' OR NEW.han_su_dung<=CURRENT_DATE)
 THEN RAISE EXCEPTION 'ACTIVE_HOLDS: release/adjust held lines before lot changes'; END IF;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_lot_stock_guard
  BEFORE UPDATE OF so_luong_ton,tinh_trang,han_su_dung
  ON lo_thuoc FOR EACH ROW EXECUTE FUNCTION pharmasiu_guard_lot_stock();


 
-- Only a recorded/simulated completed sale can issue from an eligible lot.
-- Rx requires a separately authorized approver; payment status by itself never grants Rx approval.
CREATE OR REPLACE FUNCTION pharmasiu_check_issue() RETURNS trigger AS $$
DECLARE
  issue_order RECORD;
  product_lot RECORD;
  approver_allowed BOOLEAN;
BEGIN
  IF NEW.so_luong_xuat=0 THEN RETURN NEW; END IF;
  SELECT d.trang_thai,d.kenh_ban,d.trang_thai_tt,d.thanh_toan_luc,d.giao_luc,
         d.trang_thai_toa,d.nguoi_duyet_toa_id
  INTO issue_order FROM don_hang d WHERE d.don_hang_id=NEW.don_hang_id;
  IF NOT FOUND OR (issue_order.trang_thai NOT IN ('SAN_SANG_NHAN','HOAN_TAT')
    AND NOT (issue_order.kenh_ban='POS' AND issue_order.trang_thai='CHO_XU_LY'))
      OR issue_order.trang_thai_tt NOT IN ('DEMO_DA_GHI_NHAN','DA_THANH_TOAN')
      OR issue_order.thanh_toan_luc IS NULL
  THEN RAISE EXCEPTION 'INVALID_ISSUE: payment not confirmed'; END IF;

  SELECT l.han_su_dung,l.tinh_trang,p.loai_thuoc,p.dang_ban INTO product_lot
  FROM lo_thuoc l JOIN san_pham p ON p.san_pham_id=l.san_pham_id
  WHERE l.lo_thuoc_id=NEW.lo_thuoc_id AND l.san_pham_id=NEW.san_pham_id;
  IF NOT FOUND OR product_lot.tinh_trang<>'DAT'
      OR NOT product_lot.dang_ban OR product_lot.han_su_dung<=COALESCE(issue_order.giao_luc,issue_order.thanh_toan_luc)::DATE OR product_lot.han_su_dung<=CURRENT_DATE
  THEN RAISE EXCEPTION 'INVALID_ISSUE: invalid batch or inactive product'; END IF;

  IF product_lot.loai_thuoc='RX' THEN
    SELECT COALESCE(a.co_quyen_duyet_toa,FALSE) INTO approver_allowed
    FROM tai_khoan a WHERE a.tai_khoan_id=issue_order.nguoi_duyet_toa_id;
    IF issue_order.trang_thai_toa<>'DA_DUYET' OR NOT COALESCE(approver_allowed,FALSE)
    THEN RAISE EXCEPTION 'RX_ISSUE_REQUIRES_AUTHORIZED_APPROVAL'; END IF;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_issue_guard
  BEFORE INSERT OR UPDATE OF so_luong_xuat,lo_thuoc_id,san_pham_id,don_hang_id
  ON chi_tiet_don_hang FOR EACH ROW EXECUTE FUNCTION pharmasiu_check_issue();

-- Allow payment -> issue -> handover -> complete without requiring HOAN_TAT
-- before line issuance. The completion check enforces entire-order fulfillment.
CREATE OR REPLACE FUNCTION pharmasiu_guard_completion() RETURNS trigger AS $$
BEGIN
 IF TG_OP='INSERT' THEN
   IF NEW.trang_thai='HOAN_TAT' THEN
      RAISE EXCEPTION 'COMPLETE_REQUIRES_EXISTING_PAID_ISSUED_ORDER';
   END IF;
   RETURN NEW;
 END IF;
 IF NEW.trang_thai='HOAN_TAT' AND OLD.trang_thai<>'HOAN_TAT' THEN
    IF NEW.trang_thai_tt NOT IN ('DEMO_DA_GHI_NHAN','DA_THANH_TOAN')
      OR NEW.thanh_toan_luc IS NULL OR NEW.giao_luc IS NULL
      OR NEW.so_tien_da_thu_vnd < COALESCE((
           SELECT SUM(c.so_luong*c.don_gia_chot_vnd)
           FROM chi_tiet_don_hang c WHERE c.don_hang_id=NEW.don_hang_id),0)
      OR EXISTS(SELECT 1 FROM chi_tiet_don_hang c
                JOIN lo_thuoc l ON l.lo_thuoc_id=c.lo_thuoc_id
                WHERE c.don_hang_id=NEW.don_hang_id
                  AND l.han_su_dung<=NEW.giao_luc::DATE)
      OR NOT EXISTS(SELECT 1 FROM chi_tiet_don_hang c WHERE c.don_hang_id=NEW.don_hang_id)
      OR EXISTS(SELECT 1 FROM chi_tiet_don_hang c WHERE c.don_hang_id=NEW.don_hang_id
             AND (c.so_luong_giu<>0 OR c.so_luong_xuat<>c.so_luong))
    THEN RAISE EXCEPTION 'INCOMPLETE_ISSUE_OR_HANDOFF'; END IF;
 END IF;
 IF EXISTS(SELECT 1 FROM chi_tiet_don_hang c
           WHERE c.don_hang_id=NEW.don_hang_id AND c.so_luong_xuat>0)
    AND (NEW.trang_thai_tt NOT IN ('DEMO_DA_GHI_NHAN','DA_THANH_TOAN')
         OR NEW.thanh_toan_luc IS NULL
         OR (NEW.trang_thai='HOAN_TAT' AND NEW.giao_luc IS NULL))
 THEN RAISE EXCEPTION 'ISSUED_ORDER_CANNOT_LOSE_PAYMENT_OR_HANDOFF'; END IF;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_complete_insert BEFORE INSERT ON don_hang
FOR EACH ROW EXECUTE FUNCTION pharmasiu_guard_completion();
CREATE TRIGGER trg_complete_update BEFORE UPDATE OF trang_thai,trang_thai_tt,thanh_toan_luc,giao_luc ON don_hang
FOR EACH ROW EXECUTE FUNCTION pharmasiu_guard_completion();

CREATE OR REPLACE FUNCTION pharmasiu_completed_line_immutable() RETURNS trigger AS $$
DECLARE id_ BIGINT;
BEGIN
 id_:=CASE WHEN TG_OP='INSERT' THEN NEW.don_hang_id ELSE OLD.don_hang_id END;
 IF (SELECT trang_thai FROM don_hang WHERE don_hang_id=id_)='HOAN_TAT'
 THEN RAISE EXCEPTION 'COMPLETED_ORDER_LINES_ARE_IMMUTABLE'; END IF;
 IF TG_OP='DELETE' THEN RETURN OLD; ELSE RETURN NEW; END IF;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_completed_line_guard BEFORE INSERT OR UPDATE OR DELETE
ON chi_tiet_don_hang FOR EACH ROW EXECUTE FUNCTION pharmasiu_completed_line_immutable();

-- Atomic inventory quantity movement, after the issue has passed role/expiry/status checks.
-- PostgreSQL row updates serialize concurrent changes to the same lot.
CREATE OR REPLACE FUNCTION pharmasiu_apply_issued_stock() RETURNS trigger AS $$
DECLARE delta BIGINT;
BEGIN
 IF TG_OP='INSERT' THEN delta:=NEW.so_luong_xuat;
 ELSE
   IF NEW.so_luong_xuat<OLD.so_luong_xuat
   THEN RAISE EXCEPTION 'ISSUED_QUANTITY_IMMUTABLE_NEEDS_RETURN_WORKFLOW'; END IF;
   delta:=NEW.so_luong_xuat-OLD.so_luong_xuat;
 END IF;
 IF delta<>0 THEN
    UPDATE lo_thuoc SET so_luong_ton=so_luong_ton-delta
    WHERE lo_thuoc_id=NEW.lo_thuoc_id AND san_pham_id=NEW.san_pham_id
      AND so_luong_ton>=delta;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'INSUFFICIENT_PHYSICAL_STOCK_ON_ISSUE';
    END IF;
 END IF;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_issue_stock AFTER INSERT OR UPDATE OF so_luong_xuat
ON chi_tiet_don_hang FOR EACH ROW EXECUTE FUNCTION pharmasiu_apply_issued_stock();

CREATE OR REPLACE FUNCTION pharmasiu_prevent_issued_line_retarget() RETURNS trigger AS $$
BEGIN
 IF TG_OP='DELETE' THEN
   IF OLD.so_luong_xuat>0 THEN
     RAISE EXCEPTION 'ISSUED_LINE_CANNOT_BE_DELETED';
   END IF;
   RETURN OLD;
 END IF;
 IF OLD.so_luong_xuat>0 AND
      (NEW.san_pham_id<>OLD.san_pham_id
       OR NEW.don_hang_id<>OLD.don_hang_id
       OR NEW.lo_thuoc_id IS DISTINCT FROM OLD.lo_thuoc_id)
 THEN RAISE EXCEPTION 'ISSUED_LINE_CANNOT_CHANGE_PRODUCT_LOT_OR_ORDER'; END IF;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_issued_line_retarget BEFORE UPDATE OF san_pham_id,lo_thuoc_id,don_hang_id
OR DELETE ON chi_tiet_don_hang FOR EACH ROW EXECUTE FUNCTION pharmasiu_prevent_issued_line_retarget();

 
-- Cross-row Rx approval and immutable Rx history after recorded issue.
CREATE OR REPLACE FUNCTION pharmasiu_validate_rx_approval() RETURNS trigger AS $$
DECLARE rx_issued BOOLEAN;
BEGIN
 IF NEW.trang_thai_toa='DA_DUYET' AND
     (NEW.nguoi_duyet_toa_id IS NULL OR NEW.duyet_toa_luc IS NULL
      OR NOT EXISTS(SELECT 1 FROM tai_khoan a
          WHERE a.tai_khoan_id=NEW.nguoi_duyet_toa_id
            AND a.vai_tro='CASHIER' AND a.co_quyen_duyet_toa
            AND a.trang_thai='HOAT_DONG'))
 THEN RAISE EXCEPTION 'RX_APPROVER_REQUIRED'; END IF;
 IF TG_OP='UPDATE' AND OLD.trang_thai_toa='DA_DUYET'
    AND (NEW.trang_thai_toa<>'DA_DUYET'
         OR NEW.nguoi_duyet_toa_id IS DISTINCT FROM OLD.nguoi_duyet_toa_id)
    AND EXISTS(SELECT 1 FROM chi_tiet_don_hang c JOIN san_pham p ON p.san_pham_id=c.san_pham_id
         WHERE c.don_hang_id=OLD.don_hang_id AND c.so_luong_xuat>0 AND p.loai_thuoc='RX')
 THEN RAISE EXCEPTION 'ISSUED_RX_APPROVAL_IMMUTABLE'; END IF;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_rx_approval BEFORE INSERT OR UPDATE OF trang_thai_toa,nguoi_duyet_toa_id,duyet_toa_luc
ON don_hang FOR EACH ROW EXECUTE FUNCTION pharmasiu_validate_rx_approval();

-- Moving a normal line into a completed order would bypass old-order-only guards.
CREATE OR REPLACE FUNCTION pharmasiu_stop_move_to_completed() RETURNS trigger AS $$
BEGIN
 IF (SELECT trang_thai FROM don_hang WHERE don_hang_id=NEW.don_hang_id)='HOAN_TAT'
 THEN RAISE EXCEPTION 'CANNOT_MOVE_LINES_INTO_COMPLETED_ORDER'; END IF;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_no_line_move_to_completed BEFORE UPDATE OF don_hang_id
ON chi_tiet_don_hang FOR EACH ROW EXECUTE FUNCTION pharmasiu_stop_move_to_completed();

CREATE OR REPLACE FUNCTION pharmasiu_validate_reactivated_hold() RETURNS trigger AS $$
DECLARE b RECORD; own_hold BIGINT; other_hold BIGINT;
BEGIN
 IF NEW.trang_thai NOT IN ('DA_GIU_HANG','SAN_SANG_NHAN')
    OR NEW.het_han_giu_luc IS NULL OR NEW.het_han_giu_luc<=CURRENT_TIMESTAMP THEN
   RETURN NEW;
 END IF;
 FOR b IN SELECT l.lo_thuoc_id,l.so_luong_ton FROM lo_thuoc l
          JOIN chi_tiet_don_hang c ON c.lo_thuoc_id=l.lo_thuoc_id
          WHERE c.don_hang_id=OLD.don_hang_id AND c.so_luong_giu>0
          FOR UPDATE OF l LOOP
   SELECT COALESCE(SUM(c.so_luong_giu),0) INTO own_hold
   FROM chi_tiet_don_hang c
   WHERE c.don_hang_id=OLD.don_hang_id AND c.lo_thuoc_id=b.lo_thuoc_id;
   SELECT COALESCE(SUM(c.so_luong_giu),0) INTO other_hold
   FROM chi_tiet_don_hang c JOIN don_hang d ON d.don_hang_id=c.don_hang_id
   WHERE c.lo_thuoc_id=b.lo_thuoc_id AND c.don_hang_id<>OLD.don_hang_id
     AND d.trang_thai IN ('DA_GIU_HANG','SAN_SANG_NHAN')
     AND d.het_han_giu_luc>CURRENT_TIMESTAMP;
   IF own_hold+other_hold>b.so_luong_ton
   THEN RAISE EXCEPTION 'REACTIVATED_HOLD_EXCEEDS_PHYSICAL_STOCK'; END IF;
 END LOOP;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_hold_order_reactivation BEFORE UPDATE OF trang_thai,het_han_giu_luc
ON don_hang FOR EACH ROW EXECUTE FUNCTION pharmasiu_validate_reactivated_hold();

-- Prevent unreviewed stock changes; immutable imported quantity means an
-- adjustment uses a formally approved inventory count, not an edited receipt.
CREATE OR REPLACE FUNCTION pharmasiu_prevent_receipt_edit() RETURNS trigger AS $$
BEGIN
 IF NEW.so_luong_nhap<>OLD.so_luong_nhap
 THEN RAISE EXCEPTION 'RECEIPT_QUANTITY_IMMUTABLE_USE_APPROVED_COUNT'; END IF;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_received_quantity_immutable BEFORE UPDATE OF so_luong_nhap
ON lo_thuoc FOR EACH ROW EXECUTE FUNCTION pharmasiu_prevent_receipt_edit();

CREATE OR REPLACE FUNCTION pharmasiu_verify_stock_ledger() RETURNS trigger AS $$
DECLARE expected_stock BIGINT;
BEGIN
 IF NEW.so_luong_ton=OLD.so_luong_ton THEN RETURN NEW; END IF;
 SELECT NEW.so_luong_nhap
   - COALESCE((SELECT SUM(c.so_luong_xuat) FROM chi_tiet_don_hang c
               WHERE c.lo_thuoc_id=OLD.lo_thuoc_id),0)
   + COALESCE((SELECT SUM(k.ton_sau_dieu_chinh-k.ton_truoc_dieu_chinh)
       FROM kiem_tra_lo k WHERE k.lo_thuoc_id=OLD.lo_thuoc_id
         AND k.nguoi_duyet_dieu_chinh_id IS NOT NULL),0)
 INTO expected_stock;
 IF NEW.so_luong_ton<>expected_stock
 THEN RAISE EXCEPTION 'UNAPPROVED_STOCK_ADJUSTMENT'; END IF;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_stock_must_match_ledger BEFORE UPDATE OF so_luong_ton
ON lo_thuoc FOR EACH ROW EXECUTE FUNCTION pharmasiu_verify_stock_ledger();

CREATE OR REPLACE FUNCTION pharmasiu_approve_inventory_count() RETURNS trigger AS $$
DECLARE role_ok BOOLEAN; old_stock BIGINT;
BEGIN
 IF TG_OP='UPDATE' AND OLD.nguoi_duyet_dieu_chinh_id IS NOT NULL
 THEN RAISE EXCEPTION 'APPROVED_INVENTORY_RECORD_IMMUTABLE'; END IF;
 IF NEW.nguoi_duyet_dieu_chinh_id IS NULL THEN RETURN NEW; END IF;
 SELECT (a.vai_tro='ADMIN' AND a.trang_thai IN ('DEMO','HOAT_DONG')) INTO role_ok
 FROM tai_khoan a WHERE a.tai_khoan_id=NEW.nguoi_duyet_dieu_chinh_id;
 SELECT l.so_luong_ton INTO old_stock FROM lo_thuoc l
 WHERE l.lo_thuoc_id=NEW.lo_thuoc_id FOR UPDATE;
 IF NEW.loai_kiem<>'KIEM_KE' OR NEW.ket_qua<>'CHENH_LECH'
  OR NEW.ton_truoc_dieu_chinh IS NULL OR NEW.ton_truoc_dieu_chinh<>old_stock
  OR NOT COALESCE(role_ok,FALSE)
 THEN RAISE EXCEPTION 'INVALID_INVENTORY_APPROVAL'; END IF;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_approve_count BEFORE INSERT OR UPDATE ON kiem_tra_lo
FOR EACH ROW EXECUTE FUNCTION pharmasiu_approve_inventory_count();

CREATE OR REPLACE FUNCTION pharmasiu_apply_count_approval() RETURNS trigger AS $$
BEGIN
 IF TG_OP='INSERT' THEN
  IF NEW.nguoi_duyet_dieu_chinh_id IS NULL THEN RETURN NEW; END IF;
 ELSIF OLD.nguoi_duyet_dieu_chinh_id IS NOT NULL
    OR NEW.nguoi_duyet_dieu_chinh_id IS NULL THEN RETURN NEW; END IF;
 UPDATE lo_thuoc SET so_luong_ton=NEW.ton_sau_dieu_chinh
 WHERE lo_thuoc_id=NEW.lo_thuoc_id;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_apply_count_approval AFTER INSERT OR UPDATE ON kiem_tra_lo
FOR EACH ROW EXECUTE FUNCTION pharmasiu_apply_count_approval();

CREATE OR REPLACE FUNCTION pharmasiu_prevent_approved_count_delete() RETURNS trigger AS $$
BEGIN
 IF OLD.nguoi_duyet_dieu_chinh_id IS NOT NULL
 THEN RAISE EXCEPTION 'APPROVED_INVENTORY_RECORD_CANNOT_BE_DELETED'; END IF;
 RETURN OLD;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_no_delete_approved_count BEFORE DELETE ON kiem_tra_lo
FOR EACH ROW EXECUTE FUNCTION pharmasiu_prevent_approved_count_delete();

COMMIT;
