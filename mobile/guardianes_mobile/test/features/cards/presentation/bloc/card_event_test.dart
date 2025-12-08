import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';
import 'package:guardianes_mobile/features/cards/presentation/bloc/card_event.dart';

void main() {
  group('CardEvent', () {
    group('ScanQRCodeEvent', () {
      test('should have correct props', () {
        const event = ScanQRCodeEvent(guardianId: 1, qrCode: 'test_qr');
        expect(event.props, [1, 'test_qr']);
      });

      test('should be equal with same values', () {
        const event1 = ScanQRCodeEvent(guardianId: 1, qrCode: 'test_qr');
        const event2 = ScanQRCodeEvent(guardianId: 1, qrCode: 'test_qr');
        expect(event1, event2);
      });

      test('should not be equal with different values', () {
        const event1 = ScanQRCodeEvent(guardianId: 1, qrCode: 'qr1');
        const event2 = ScanQRCodeEvent(guardianId: 1, qrCode: 'qr2');
        expect(event1, isNot(event2));
      });
    });

    group('LoadCardCollectionEvent', () {
      test('should have correct props', () {
        const event = LoadCardCollectionEvent(guardianId: 42);
        expect(event.props, [42]);
      });

      test('should be equal with same guardianId', () {
        const event1 = LoadCardCollectionEvent(guardianId: 1);
        const event2 = LoadCardCollectionEvent(guardianId: 1);
        expect(event1, event2);
      });
    });

    group('LoadCollectionStatisticsEvent', () {
      test('should have correct props', () {
        const event = LoadCollectionStatisticsEvent(guardianId: 5);
        expect(event.props, [5]);
      });

      test('should be equal with same guardianId', () {
        const event1 = LoadCollectionStatisticsEvent(guardianId: 10);
        const event2 = LoadCollectionStatisticsEvent(guardianId: 10);
        expect(event1, event2);
      });
    });

    group('SearchCardsEvent', () {
      test('should have correct props with all parameters', () {
        const event = SearchCardsEvent(
          name: 'Dragon',
          element: CardElement.fire,
          rarity: CardRarity.legendary,
        );
        expect(event.props, ['Dragon', CardElement.fire, CardRarity.legendary]);
      });

      test('should have correct props with null parameters', () {
        const event = SearchCardsEvent();
        expect(event.props, [null, null, null]);
      });

      test('should be equal with same values', () {
        const event1 = SearchCardsEvent(name: 'Test');
        const event2 = SearchCardsEvent(name: 'Test');
        expect(event1, event2);
      });
    });

    group('LoadRecentCardsEvent', () {
      test('should have correct props with default limit', () {
        const event = LoadRecentCardsEvent(guardianId: 1);
        expect(event.props, [1, 10]);
      });

      test('should have correct props with custom limit', () {
        const event = LoadRecentCardsEvent(guardianId: 1, limit: 5);
        expect(event.props, [1, 5]);
      });

      test('should be equal with same values', () {
        const event1 = LoadRecentCardsEvent(guardianId: 1, limit: 5);
        const event2 = LoadRecentCardsEvent(guardianId: 1, limit: 5);
        expect(event1, event2);
      });
    });
  });
}
